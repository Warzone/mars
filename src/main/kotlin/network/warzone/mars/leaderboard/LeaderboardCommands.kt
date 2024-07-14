package network.warzone.mars.leaderboard

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.Mars
import network.warzone.mars.player.commands.ModCommands
import network.warzone.mars.player.models.PlayerStats
import network.warzone.mars.utils.enumify
import network.warzone.mars.utils.getUsername
import network.warzone.mars.utils.strategy.multiLine
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import tc.oc.pgm.api.PGM
import java.util.*
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
        }.thenApply { lbEntries ->
            val message = audience.multiLine()
            lbEntries.mapIndexed { idx, entry ->
                val username = getUsername(UUID.fromString(entry.id), entry.name, match, offlineNameProvider = ModCommands.offlineNameProvider)
                Component.text("${idx + 1}. ", NamedTextColor.GOLD)
                    .append(username).append(Component.text(": ", NamedTextColor.GRAY))
                    .append(formatScore(scoreTypeAndPeriod.first, entry.score))
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

    private fun formatScore(scoreType: LeaderboardScoreType, score: Int) : Component =
        when (scoreType) {
            LeaderboardScoreType.XP -> {
                Component.text(score, NamedTextColor.YELLOW).append(
                    Component.text(" (Level: ${PlayerStats.EXP_FORMULA.getLevelFromExp(score.toDouble())})",
                        NamedTextColor.GRAY)
                )
            }
            else -> Component.text(score, NamedTextColor.YELLOW)
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
}