package network.warzone.mars.player.perks

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import network.warzone.mars.utils.ItemUtils
import network.warzone.mars.utils.color
import network.warzone.mars.utils.menu.GUI
import network.warzone.mars.utils.menu.gui
import network.warzone.mars.utils.menu.item
import network.warzone.mars.utils.parseHttpException
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tc.oc.pgm.util.material.Materials

object JoinSoundService {

    private val ORB_SOUND = Sound.sound(Key.key("random.orb"), Sound.Source.MASTER, 0.05f, 1f)
    private val NO_SOUND = Sound.sound(Key.key("entity.villager.no"), Sound.Source.MASTER, 0.25f, 1f)

    private var joinSounds: List<JoinSound>? = null

    init {
        runBlocking {
            joinSounds = ApiClient.get<List<JoinSoundData>>("/mc/perks/join_sounds").mapNotNull { sound ->
                val bukkitSound = Sound.sound(Key.key(sound.sound), Sound.Source.MASTER, sound.volume, sound.pitch)
                val material = ItemUtils.getMaterialByName(sound.guiIcon) ?: Materials.SIGN
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

    private fun playSound(sound: JoinSound, filter: (Player) -> Boolean) {
        Bukkit.getOnlinePlayers()
            .filter(filter)
            .map(AUDIENCE_PROVIDER::player)
            .forEach { it.playSound(sound.bukkitSound) }
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
                            val audience = AUDIENCE_PROVIDER.player(actor)
                            val sound = if (hasPermission) {
                                ORB_SOUND // default
                            } else {
                                NO_SOUND
                            }
                            audience.playSound(sound)
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
