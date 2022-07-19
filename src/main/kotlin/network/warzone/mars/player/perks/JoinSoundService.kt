package network.warzone.mars.player.perks

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.ItemUtils
import network.warzone.mars.utils.enumify
import network.warzone.mars.utils.menu.GUI
import network.warzone.mars.utils.menu.Item
import network.warzone.mars.utils.menu.gui
import network.warzone.mars.utils.menu.item
import network.warzone.mars.utils.parseHttpException
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object JoinSoundService {
    private var joinSounds: List<JoinSoundParsed>? = null
    private val soundNames: Set<String> = Sound.values().map { it.name }.toSet()

    init {
        runBlocking {
            joinSounds = ApiClient.get<List<JoinSound>>("/mc/perks/join_sounds").mapNotNull { sound ->
                val formattedSoundName = sound.sound.enumify()
                val bukkitSound = (if (formattedSoundName in soundNames) Sound.valueOf(formattedSoundName) else null)
                    ?: return@mapNotNull null
                val material = ItemUtils.getMaterialByName(sound.guiIcon) ?: Material.SIGN
                val item = ItemStack(material)
                return@mapNotNull JoinSoundParsed(sound.id, sound.name, bukkitSound, sound.permission, item,
                    sound.guiSlot, sound.volume, sound.pitch)
            }
        }
    }

    suspend fun setActiveSound(player: String, soundId: String?): PlayerProfile {
        val request = parseHttpException {
            ApiClient.post<PlayerProfile, JoinSoundSetRequest>(
                "/mc/perks/join_sounds/$player/sound",
                JoinSoundSetRequest(soundId)
            )
        }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    fun getSoundById(id: String): JoinSoundParsed? =
        joinSounds?.firstOrNull { sound -> sound.id == id }

    fun getJoinSoundGUI(player: Player) : GUI =
        gui("${ChatColor.DARK_GREEN}Join Sounds", 4) {
            this@JoinSoundService.joinSounds?.forEach { joinSound ->
                slot(joinSound.guiSlot) {
                    item = item(joinSound.guiIcon.type) { name = joinSound.name }
                    onclick = {
                        Mars.async {
                            val playerProfile = PlayerFeature.get(player.name) ?: return@async
                            if (playerProfile.activeJoinSoundId != joinSound.id) {
                                setActiveSound(player.name, joinSound.id)
                                playerProfile.activeJoinSoundId = joinSound.id
                            }
                            player.sendMessage(
                                "Selected join sound ${ChatColor.GREEN}${joinSound.name}"
                            )
                            player.closeInventory()
                        }
                    }
                }
            }

            slot(0) {
                item = item(Material.BARRIER) { name = "${ChatColor.RED}Remove Join Sound" }
                onclick = {
                    Mars.async {
                        val playerProfile = PlayerFeature.getCached(player.name) ?: return@async
                        if (playerProfile.activeJoinSoundId != null) {
                            setActiveSound(player.name, null)
                            playerProfile.activeJoinSoundId = null
                        }
                        player.sendMessage(
                            "${ChatColor.RED}Unselected join sound"
                        )
                        player.closeInventory()
                    }
                }
            }
        }

    data class JoinSoundSetRequest(val activeJoinSoundId: String? = null)
}
