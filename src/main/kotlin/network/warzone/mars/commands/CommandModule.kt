package network.warzone.mars.commands

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.parametric.AbstractModule
import app.ashcon.intake.parametric.Provider
import network.warzone.mars.Mars
import network.warzone.mars.broadcast.Broadcast
import network.warzone.mars.commands.providers.*
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.function.Supplier
import kotlin.reflect.KClass

object CommandModule : AbstractModule(), Listener {
    init {
        Mars.registerEvents(this)
    }

    override fun configure() {
        bind(Audience::class, AudienceProvider())
        bind(Rank::class, RankProvider())
        bind(Tag::class, TagProvider())
        bind(PlayerContext::class, PlayerContextProvider())
        bind(Punishment::class, PunishmentProvider())
        bind(PlayerProfile::class, PlayerProfileProvider())
        bind(Broadcast::class, BroadcastProvider())
    }

    private fun <T : Any> bind(type: KClass<T>, provider: Provider<T>) {
        bind(type.java).toProvider(provider)
    }

    private fun <T : Any> bind(type: KClass<T>, supplier: Singleton<T>) {
        bind(type, supplier)
    }

    @FunctionalInterface
    private interface Singleton<T> : Provider<T>, Supplier<T> {
        override fun get(args: CommandArgs, list: MutableList<out Annotation>): T {
            return get()
        }

        override fun isProvided(): Boolean {
            return true
        }
    }

    val punishCommands = listOf(
        "/ban",
        "/mute",
        "/kick",
        "/pardon",
        "/baninfo",
        "/banip",
        "/pgm:ban",
        "/pgm:baninfo",
        "/pgm:banip",
        "/pgm:kick",
        "/pgm:mute",
        "/warn",
        "/pgm:warn",
        "/pgm:mutes",
        "/mutes",
        "/pgm:mutelist",
        "/mutelist",
        "/permban",
        "/pgm:permban"
    )

    val miscCommands =
        listOf("/msg", "/pgm:msg", "/pgm:tell", "/w", "/pgm:w", "/pm", "/dm", "/pgm:pm", "/pgm:dm", "/tell")

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val isPunishCommand = punishCommands.any { event.message.toLowerCase().startsWith(it) }
        if (isPunishCommand) {
            event.isCancelled = true
            event.player.sendMessage("${ChatColor.RED}This command has been disabled in favour of /punish.")
        }

        val isBlockedCommand = miscCommands.any { event.message.toLowerCase().startsWith(it) }
        if (isBlockedCommand) {
            event.isCancelled = true
            event.player.sendMessage("${ChatColor.RED}This command is unavailable.")
        }
    }
}