package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import network.warzone.mars.commands.providers.PlayerName
import network.warzone.mars.match.tracker.KillstreakTracker
import network.warzone.mars.player.feature.LevelColorService
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.audience
import network.warzone.mars.utils.matchPlayer
import network.warzone.mars.utils.translate
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.*
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import javax.annotation.Nullable


class StatCommands {
    @Command(aliases = ["killstreak", "ks"], desc = "View your current killstreak", usage = "[player]")
    fun onKillstreakView(@Sender sender: Player, @Nullable @PlayerName playerName: String?) {
        val player = playerName?.let { Bukkit.getPlayer(it) } ?: sender
        val killstreak = KillstreakTracker.getKillstreak(player.uniqueId ?: sender.uniqueId)
            ?: return sender.audience.sendMessage(translatable("command.stat.killstreak.untracked", NamedTextColor.RED))
        val (_, color) = KillstreakTracker.getNearestTrackedKillstreak(killstreak)
        if (killstreak > 0) {
            if (player == sender) {
                sender.audience.sendMessage(
                    translatable("command.stat.killstreak.self", NamedTextColor.GREEN,
                        text(killstreak, color, TextDecoration.BOLD))
                )
            } else {
                sender.audience.sendMessage(
                    translatable("command.stat.killstreak.other", NamedTextColor.GREEN,
                        text(player.name),
                        text(killstreak.toString(), color, TextDecoration.BOLD))
                )
            }
        } else {
            if (player == sender) {
                sender.audience.sendMessage(translatable("command.stat.killstreak.self.none", NamedTextColor.RED))
            } else {
                sender.audience.sendMessage(
                    translatable("command.stat.killstreak.other.none", NamedTextColor.RED, text(player.name))
                )
            }
        }
    }

    @Command(aliases = ["stats", "statistics"], desc = "View a player's stats", usage = "[player]")
    fun onStatsView(@Sender sender: Player, @Nullable @PlayerName playerName: String?) {
        Mars.async {
            if (playerName == null) {
                viewStats(sender.matchPlayer, PlayerFeature.fetch(sender.name) ?: throw CommandException(
                    translate("command.stat.stats.error", sender)))
            } else {
                val profile = PlayerFeature.fetch(playerName)
                    ?: PlayerFeature.fetch(sender.name)
                    ?: throw CommandException(translate("command.stat.stats.error", sender))
                viewStats(sender.matchPlayer, profile)
            }
        }
    }

    private fun viewStats(sender: MatchPlayer, profile: PlayerProfile) {
        val stats = profile.stats
        val outline = text("-------------------------------", NamedTextColor.BLUE, TextDecoration.STRIKETHROUGH)
        sender.sendMessage(outline)
        val component =
            text()
                .append(
                    space(),
                    space(),
                    translatable("command.stat.stats.title", NamedTextColor.DARK_AQUA,
                        text(
                            profile.name,
                            NamedTextColor.AQUA
                        )
                    ).append(newline())
                )
                .append { newline() }
                .append {
                    createCustomStat(
                        "command.stat.stats.label.level",
                        stats.level,
                        LevelColorService.chatColorFromLevel(stats.level)
                    )
                }
                .append { createLabelledStat("command.stat.stats.label.xp", stats.xp, StatType.NEUTRAL) }
                .append { newline() }
                .append { createLabelledStat("command.stat.stats.label.kills", stats.kills, StatType.POSITIVE) }
                .append { createLabelledStat("command.stat.stats.label.first-bloods", stats.firstBloods, StatType.POSITIVE) }
                .append { createLabelledStat("command.stat.stats.label.deaths", stats.deaths, StatType.NEGATIVE) }
                .append { createLabelledStat("command.stat.stats.label.kdr", stats.kdr, StatType.NEUTRAL) }
                .append { newline() }
                .append { createLabelledStat("command.stat.stats.label.wins", stats.wins, StatType.POSITIVE) }
                .append { createLabelledStat("command.stat.stats.label.losses", stats.losses, StatType.NEGATIVE) }
                .append { createLabelledStat("command.stat.stats.label.wr", stats.winPercentage, StatType.NEUTRAL) }
                .append { outline }
        sender.sendMessage(component)
    }

    enum class StatType(val color: NamedTextColor) {
        POSITIVE(NamedTextColor.GREEN),
        NEGATIVE(NamedTextColor.RED),
        NEUTRAL(NamedTextColor.AQUA)
    }

    private fun createLabelledStat(label: String, value: Any, type: StatType): Component {
        return createCustomStat(label, value, type.color)
    }

    private fun createCustomStat(label: String, value: Any, color: NamedTextColor): Component {
        return text()
            .append { space() }
            .append { space() }
            .append { translatable(label, NamedTextColor.DARK_AQUA, text(value.toString(), color)) }
            .append { newline() }
            .build()
    }
}