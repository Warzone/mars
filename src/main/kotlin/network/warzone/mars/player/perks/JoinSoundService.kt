package network.warzone.mars.player.perks

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.ItemUtils
import network.warzone.mars.utils.color
import network.warzone.mars.utils.enumify
import network.warzone.mars.utils.menu.GUI
import network.warzone.mars.utils.menu.gui
import network.warzone.mars.utils.menu.item
import network.warzone.mars.utils.parseHttpException
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object JoinSoundService {
    private var joinSounds: List<JoinSound>? = null
    private val soundNames: Set<String> = Sound.values().map { it.name }.toSet()

    init {
        runBlocking {
            joinSounds = ApiClient.get<List<JoinSoundData>>("/mc/perks/join_sounds").mapNotNull { sound ->
                val formattedSoundName = sound.sound.enumify()
                val bukkitSound = (if (formattedSoundName in soundNames) Sound.valueOf(formattedSoundName) else null)
                    ?: return@mapNotNull null
                val material = ItemUtils.getMaterialByName(sound.guiIcon) ?: Material.SIGN
                val item = ItemStack(material)
                return@mapNotNull JoinSound(sound.id, sound.name, sound.description, bukkitSound, sound.permission, item,
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

    fun getSoundById(id: String): JoinSound? =
        joinSounds?.firstOrNull { sound -> sound.id == id }

    fun playSound(sound: JoinSound) = playSound(sound) { true }

    fun playSound(sound: JoinSound, filter: (Player) -> Boolean) {
        Bukkit.getOnlinePlayers()
            .filter(filter)
            .forEach { it.playSound(it.location, sound.bukkitSound, sound.volume, sound.pitch) }
    }

    fun getJoinSoundGUI(playerContext: PlayerContext) : GUI = runBlocking {
        val player = playerContext.player
        val playerProfile = playerContext.getPlayerProfile()
        return@runBlocking gui("${ChatColor.DARK_GREEN}Join Sounds", 4) {
            this@JoinSoundService.joinSounds?.forEach { joinSound ->
                val hasPermission = player.hasPermission(joinSound.permission)
                slot(joinSound.guiSlot) {
                    item = item(joinSound.guiIcon.type) {
                        name = "${ChatColor.RESET}${joinSound.name.color()}"

                        val description = if (joinSound.description.isEmpty()) {
                            emptyArray<String>()
                        } else {
                            arrayOf(
                                "",
                                *joinSound.description.map { it.color() }.toTypedArray()
                            )
                        }

                        val selection = if (hasPermission) {
                            if (joinSound.id == playerProfile.activeJoinSoundId) {
                                "${ChatColor.GREEN}Selected"
                            } else {
                                "${ChatColor.YELLOW}Click to select"
                            }
                        } else {
                            "${ChatColor.RED}No permission!"
                        }

                        lore = listOf(
                            *description,
                            "",
                            "${ChatColor.YELLOW}Right Click to preview!",
                            "",
                            selection
                        )

                    }
                    onclick = {
                        if (!this.isRightClick) {
                            if (hasPermission) {
                                Mars.async {
                                    if (playerProfile.activeJoinSoundId != joinSound.id) {
                                        setActiveSound(player.name, joinSound.id)
                                        playerProfile.activeJoinSoundId = joinSound.id
                                    }
                                    player.sendMessage(
                                        "Selected join sound ${ChatColor.GREEN}${joinSound.name.color()}"
                                    )
                                    player.closeInventory()
                                }
                            } else {
                                player.sendMessage(
                                    "${ChatColor.RED}You don't have permission to use ${ChatColor.GREEN}${joinSound.name.color()}"
                                )
                                player.closeInventory()
                            }
                        }
                    }
                    sound = { actor ->
                        if (this.isRightClick) {
                            this@JoinSoundService.playSound(joinSound) { it == actor }
                        } else {
                            if (hasPermission) {
                                actor.playSound(actor.location, Sound.ORB_PICKUP, .05f, 1f) // default
                            } else {
                                actor.playSound(actor.location, Sound.VILLAGER_NO, .25f, 1f)
                            }
                        }
                    }
                }
            }

            slot(0) {
                item = item(Material.BARRIER) { name = "${ChatColor.RED}Remove Join Sound" }
                onclick = {
                    Mars.async {
                        if (playerProfile.activeJoinSoundId != null) {
                            setActiveSound(player.name, null)
                            playerProfile.activeJoinSoundId = null
                        }
                        player.sendMessage(
                            "${ChatColor.YELLOW}Cleared join sound."
                        )
                        player.closeInventory()
                    }
                }
            }
        }
    }

    data class JoinSoundSetRequest(val activeJoinSoundId: String? = null)
}
