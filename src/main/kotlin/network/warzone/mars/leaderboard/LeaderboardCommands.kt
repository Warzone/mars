package network.warzone.mars.leaderboard

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.Mars
import network.warzone.mars.player.commands.ModCommands
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.player.models.PlayerStats
import network.warzone.mars.utils.enumify
import network.warzone.mars.utils.getUsername
import network.warzone.mars.utils.strategy.multiLine
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import tc.oc.pgm.api.PGM
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.annotation.Nullable

class LeaderboardCommands {
    @Command(aliases = ["leaderboard", "lb"], desc = "View leaderboards", usage = "[scoreType] [period]")
    fun onLeaderboardList(
        @Sender sender: CommandSender,
        @Nullable scoreType: String? = null,
        @Nullable period: String? = null
    ) {
        val match = PGM.get().matchManager.getMatch(sender)!!
        val audience: tc.oc.pgm.util.Audience = tc.oc.pgm.util.Audience.get(sender) // PGM Audience required for name formatting
        val scoreTypeAndPeriod = validateScoreTypeAndPeriod(scoreType, period)
        if (scoreTypeAndPeriod == null) {
            sender.sendMessage("${ChatColor.RED}/lb " +
                "${formatVariantsToUsageString(LeaderboardScoreType::class.java)} " +
                "${formatVariantsToUsageString(LeaderboardPeriod::class.java)}"
            )
            return
        }
        Mars.asyncAsFutureWithResult {
            LeaderboardClient.fetchLeaderboardEntries(scoreTypeAndPeriod.first, scoreTypeAndPeriod.second)
        }.thenApply { lbRawEntries ->
            val playerFutures = lbRawEntries.mapIndexed { idx, entry ->
                val standing = idx + 1
                Mars.asyncAsFutureWithResult {
                    val player = PlayerFeature.fetch(entry.name)
                    LeaderboardEntryData(player, entry, standing)
                }
            }
            CompletableFuture.allOf(*playerFutures.toTypedArray()).thenApply {
                val lbEntries = playerFutures
                    .map { f -> f.join() }
                    .sortedBy { data -> data.standing  }
                    val message = audience.multiLine()
                    lbEntries.mapIndexed { idx, entry ->
                        val (entryPlayer, lbEntry) = entry
                        val username = getUsername(UUID.fromString(lbEntry.id), lbEntry.name, match, offlineNameProvider = ModCommands.offlineNameProvider)
                        Component.text("${entry.standing}. ", NamedTextColor.GOLD)
                            .append(username).append(Component.text(": ", NamedTextColor.GRAY))
                            .append(formatScore(scoreTypeAndPeriod.first, scoreTypeAndPeriod.second, lbEntry.score, entryPlayer))
                    }.forEach {
                        message.appendMultiLineComponent(it)
                    }
                    audience.sendMessage(
                        Component.text(
                            "${scoreTypeAndPeriod.first.name} leaderboard (${scoreTypeAndPeriod.second.name})",
                            NamedTextColor.GREEN
                        )
                    )
                    message.deliver()
            }
        }
    }

    private fun formatScore(scoreType: LeaderboardScoreType, period: LeaderboardPeriod, score: Int, player: PlayerProfile?) : Component =
        when (scoreType) {
            LeaderboardScoreType.XP -> {
                Component.text(score, NamedTextColor.YELLOW)
                    .append(Component.space())
                    .append(
                        if (period == LeaderboardPeriod.ALL_TIME)
                            Component.text(
                                " (Level: ${PlayerStats.EXP_FORMULA.getLevelFromExp(score.toDouble())})",
                            NamedTextColor.GRAY
                            )
                        else getLevelsGainedFormatted(score, player)
                    )
            }
            else -> Component.text(score, NamedTextColor.YELLOW)
        }

    private fun getLevelsGainedFormatted(differential: Int, player: PlayerProfile?) : Component {
        if (player == null) return Component.empty()
        val playerXp = player.stats.xp
        val prevLevel = PlayerStats.EXP_FORMULA.getLevelFromExp((playerXp - differential).toDouble())
        val currentLevel = PlayerStats.EXP_FORMULA.getLevelFromExp(playerXp.toDouble())
        val gained = currentLevel - prevLevel
        if (gained == 0) {
            return Component.text("(No levels gained)", NamedTextColor.GRAY)
        }
        return Component.text("(Gained ${gained} level${if (gained == 1) "" else "s"})", NamedTextColor.LIGHT_PURPLE)
    }

    private fun validateScoreTypeAndPeriod(scoreType: String?, period: String?) : Pair<LeaderboardScoreType, LeaderboardPeriod>? {
        if (scoreType == null) return null
        val scoreType = getEnumVariant<LeaderboardScoreType>(scoreType)
        val period = if (period == null) LeaderboardPeriod.ALL_TIME else getEnumVariant<LeaderboardPeriod>(period)
        if (scoreType == null || period == null) return null
        return scoreType to period
    }

    private fun <T : Enum<T>> formatVariantsToUsageString(enumClazz: Class<T>) : String {
        return "(" + enumClazz.enumConstants.joinToString(separator = "|") { it.name.toLowerCase() } + ")"
    }

    private inline fun <reified T : Enum<T>> getEnumVariant(name: String): T? {
        val enumified = name.enumify()
        return enumValues<T>().firstOrNull { it.name == enumified }
    }

    private data class LeaderboardEntryData(
        val player: PlayerProfile?,
        val entry: LeaderboardEntry,
        val standing: Int
    )
}