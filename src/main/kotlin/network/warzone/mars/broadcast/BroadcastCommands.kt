package network.warzone.mars.broadcast

import app.ashcon.intake.Command
import app.ashcon.intake.parametric.annotation.Text
import network.warzone.mars.Mars
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class BroadcastCommands {
    @Command(aliases = ["raw"], desc = "Broadcast a custom message", usage = "<message>", perms = ["mars.broadcast"])
    fun onRawBroadcast(sender: CommandSender, @Text message: String) {
        BroadcastFeature.broadcast(message, true, null)
    }

    @Command(aliases = ["preset"], desc = "Broadcast a pre-set message", usage = "<broadcast>", perms = ["mars.broadcast"])
    fun onPresetBroadcast(sender: CommandSender, broadcast: Broadcast) {
        BroadcastFeature.broadcast(broadcast.message, broadcast.newline, broadcast.permission)
    }

    @Command(aliases = ["auto", "toggle"], desc = "Toggle auto broadcasts", perms = ["mars.broadcast"])
    fun onToggleBroadcast(sender: CommandSender) {
        val auto = BroadcastFeature.autoBroadcast
        if (auto) sender.sendMessage("${ChatColor.GREEN}Disabled auto broadcasting")
        else sender.sendMessage("${ChatColor.GREEN}Enabled auto broadcasting")
        BroadcastFeature.autoBroadcast = !auto
    }

    @Command(aliases = ["reload"], desc = "Reload the broadcast list", perms = ["mars.broadcast"])
    fun onReloadBroadcast(sender: CommandSender) {
        Mars.async {
            BroadcastFeature.reload()
            sender.sendMessage("${ChatColor.GREEN}Reloaded broadcasts")
        }
    }
}