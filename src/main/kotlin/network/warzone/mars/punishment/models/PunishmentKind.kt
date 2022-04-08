package network.warzone.mars.punishment.models

import org.bukkit.ChatColor
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable

enum class PunishmentKind(val color: ChatColor, val key: String, val noun: String, val verb: String, val pastTense: String) {
    WARN(ChatColor.YELLOW, "warn", "Warning", "Warn", "warned"),
    KICK(ChatColor.YELLOW, "kick", "Kick", "Kick", "kicked"),
    MUTE(ChatColor.GOLD, "mute", "Mute", "Mute", "muted"),
    BAN(ChatColor.RED, "ban", "Ban", "Ban", "banned"),
    IP_BAN(ChatColor.DARK_RED, "ipban", "IP Ban", "IP Ban", "IP banned");

    fun nounKey() = "punishment.${key}.noun"
    fun verbKey() = "punishment.${key}.verb"
    fun pastTenseKey() = "punishment.${key}.verb.past"

    fun noun(): Component = translatable(nounKey())
    fun verb(): Component = translatable(verbKey())
    fun pastTense(): Component = translatable(pastTenseKey())

}