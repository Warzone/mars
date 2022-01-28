package network.warzone.mars.player.settings

import network.warzone.mars.utils.matchPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import tc.oc.pgm.api.setting.SettingKey
import tc.oc.pgm.api.setting.SettingValue

class SettingsListener : Listener {
    private val setSettingCommands = listOf("/set", "/setting", "/pgm:set", "/pgm:setting")
    private val forcedSettings = mapOf(
        listOf("stats") to "on",
        listOf("join", "jms") to "none"
    )

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.matchPlayer.settings.setValue(SettingKey.JOIN, SettingValue.JOIN_OFF)
        event.player.matchPlayer.settings.setValue(SettingKey.STATS, SettingValue.STATS_ON)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSettingSet(event: PlayerCommandPreprocessEvent) {
        val split = event.message.toLowerCase().split(" ")

        val command = split.first()
        val isSetCommand = setSettingCommands.any { command == it }
        if (!isSetCommand) return

        if (split.size >= 2) {
            val setting = split[1]
            val forcedKey = forcedSettings.keys.find { it.contains(setting) } ?: return
            val forcedValue = forcedSettings[forcedKey]
            event.message = "/set $setting $forcedValue"
        }
    }
}