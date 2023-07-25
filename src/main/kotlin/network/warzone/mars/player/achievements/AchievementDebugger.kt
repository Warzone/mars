package network.warzone.mars.player.achievements

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

interface AchievementDebugger {
    val debugPrefix: String
        get() = ChatColor.DARK_GRAY.toString() + "[@] " + ChatColor.GRAY.toString()

    val MewTwoKing: Player?
        get() = Bukkit.getPlayer("MewTwoKing")

    fun sendDebugMessage(message: String) {
        MewTwoKing?.sendMessage(debugPrefix + message)
    }

    fun sendConsoleMessage(message: String) {
        println("[AchievementDebugger] $message")
    }
}