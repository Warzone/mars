package network.warzone.mars.punishment.models

import org.bukkit.ChatColor

enum class PunishmentKind(val colour: ChatColor, val noun: String, val verb: String, val pastTense: String) {
    WARN(ChatColor.YELLOW, "Warning", "Warn", "warned"),
    KICK(ChatColor.YELLOW, "Kick", "Kick", "kicked"),
    MUTE(ChatColor.GOLD, "Mute", "Mute", "muted"),
    BAN(ChatColor.RED, "Ban", "Ban", "banned"),
    IP_BAN(ChatColor.DARK_RED, "IP Ban", "IP Ban", "IP banned")
}