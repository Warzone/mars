package network.warzone.mars.commands

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.parametric.AbstractModule
import app.ashcon.intake.parametric.Provider
import net.kyori.adventure.audience.Audience
import network.warzone.mars.commands.providers.AudienceProvider
import network.warzone.mars.commands.providers.PlayerContextProvider
import network.warzone.mars.commands.providers.RankProvider
import network.warzone.mars.commands.providers.TagProvider
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.ranks.models.Rank
import network.warzone.mars.tags.models.Tag
import java.util.function.Supplier
import kotlin.reflect.KClass

object CommandModule : AbstractModule() {

    override fun configure() {
        bind(Audience::class, AudienceProvider())
        bind(Rank::class, RankProvider())
        bind(Tag::class, TagProvider())
        bind(PlayerContext::class, PlayerContextProvider())
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