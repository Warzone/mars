package network.warzone.mars.commands

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.parametric.AbstractModule
import app.ashcon.intake.parametric.Provider
import network.warzone.mars.broadcast.Broadcast
import network.warzone.mars.commands.providers.*
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import java.util.function.Supplier
import kotlin.reflect.KClass

object CommandModule : AbstractModule() {
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
}