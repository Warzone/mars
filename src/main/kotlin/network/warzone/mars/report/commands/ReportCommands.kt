package network.warzone.mars.report.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import app.ashcon.intake.parametric.annotation.Default
import app.ashcon.intake.parametric.annotation.Switch
import app.ashcon.intake.parametric.annotation.Text
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent.showText
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import network.warzone.mars.report.PlayerReportEvent
import network.warzone.mars.report.Report
import network.warzone.mars.utils.matchPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.Permissions
import tc.oc.pgm.api.integration.Integration
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.listeners.ChatDispatcher
import tc.oc.pgm.util.Audience
import tc.oc.pgm.util.PrettyPaginatedComponentResults
import tc.oc.pgm.util.named.NameStyle
import tc.oc.pgm.util.text.TemporalComponent
import tc.oc.pgm.util.text.TextFormatter
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Ported from
 * https://github.com/PGMDev/PGM/blob/82b6deba1fc6d8bf90cde7ca881e01cee0540ded/core/src/main/java/tc/oc/pgm/community/command/ReportCommand.java
 */
class ReportCommands {

    companion object {
        private val REPORT_NOTIFY_SOUND: Sound = sound(key("random.pop"), Sound.Source.MASTER, 1f, 1.2f)

        private const val REPORT_COOLDOWN_SECONDS: Long = 15
        private const val REPORT_EXPIRE_HOURS:  Long = 1

        private val LAST_REPORT_SENT: Cache<UUID, Instant> =
            CacheBuilder.newBuilder().expireAfterWrite(REPORT_COOLDOWN_SECONDS, TimeUnit.SECONDS).build()

        private val RECENT_REPORTS: Cache<UUID, Report> =
            CacheBuilder.newBuilder().expireAfterWrite(REPORT_EXPIRE_HOURS, TimeUnit.HOURS).build()
    }

    @Command(aliases = ["report"], usage = "<player> <reason>", desc = "Report a player who is breaking the rules")
    fun report(@Sender sender: Player, target: Player, @Text reason: String) {
        val matchPlayer = sender.matchPlayer
        val match = matchPlayer.match
        val needsCooldown = !sender.hasPermission(Permissions.STAFF)
        if (needsCooldown) {
            // Check for cooldown
            val lastReport: Instant? = LAST_REPORT_SENT.getIfPresent(sender.uniqueId)
            if (lastReport != null) {
                val timeSinceReport: Duration = Duration.between(lastReport, Instant.now())
                val secondsRemaining: Long = REPORT_COOLDOWN_SECONDS - timeSinceReport.getSeconds()
                if (secondsRemaining > 0) {
                    val secondsComponent: TextComponent = text(secondsRemaining.toString())
                    val secondsLeftComponent: TextComponent = text()
                        .append(secondsComponent)
                        .append(text(if (secondsRemaining != 1L) "misc.seconds" else "misc.second")).build()
                    matchPlayer.sendWarning(text("Please wait ").append(secondsLeftComponent).append(text(" before running that command again")))
                    return
                }
            }
        }
        val accused: MatchPlayer? = match.getPlayer(target)

        if (accused == null || Integration.isVanished(accused.bukkit)) {
            matchPlayer.sendWarning(
                text("Could not find player named '" + target.getName() + "'", NamedTextColor.RED)
            )
            return
        }
        val event = PlayerReportEvent(accused, matchPlayer, reason)
        match.callEvent(event)
        if (event.isCancelled()) {
            return
        }
        val thanks: TextComponent = text("Thank you.", NamedTextColor.GREEN)
            .append(space())
            .append(text("The issue will be dealt with shortly.", NamedTextColor.GOLD))
        matchPlayer.sendMessage(thanks)

        val component: Component = text()
                .append(matchPlayer.getName(NameStyle.FANCY))
                .append(text(" reported ", NamedTextColor.YELLOW))
                .append(accused.getName(NameStyle.FANCY))
                .append(text(": ", NamedTextColor.YELLOW))
                .append(text(reason.trim(), NamedTextColor.WHITE)).build()

        val id = UUID.randomUUID()
        RECENT_REPORTS.put(
            id,
            Report(
                _id = id,
                target = accused.getId(),
                sender = sender.uniqueId,
                targetName = accused.getBukkit().getName(),
                senderName = sender.getName(),
                reason = reason
            )
        )
        if (needsCooldown) {
            LAST_REPORT_SENT.put(sender.uniqueId, Instant.now())
        }
        ChatDispatcher.broadcastAdminChatMessage(component, match, Optional.of(REPORT_NOTIFY_SOUND))
    }

    @Command(
        aliases = ["reports", "reps", "reporthistory"],
        desc = "Display a list of recent reports",
        usage = "(page) -t [target player]",
        flags = "t",
        perms = [Permissions.STAFF]
    )
    fun reportHistory(
        @Sender sender: CommandSender,
        @Default("1") page: Int,
        @Switch('t') target: String?
    ) {
        val audience = Audience.get(sender)
        val match = PGM.get().matchManager.getMatch(sender)
        if (RECENT_REPORTS.size() == 0L) {
            audience.sendMessage(text("There have been no recent reports!", NamedTextColor.RED))
            return
        }
        var reportList: List<Report> = RECENT_REPORTS.asMap().values.stream().collect(Collectors.toList())
        if (target != null) {
            reportList = reportList.stream()
                .filter { r -> r.targetName.equals(target, ignoreCase = true) }
                .collect(Collectors.toList())
        }
        Collections.sort(reportList) // Sort list
        Collections.reverse(reportList) // Reverse so most recent show up first
        val headerResultCount: Component = text(reportList.size.toString(), NamedTextColor.RED)
        val perPage = 6
        val pages: Int = (reportList.size + perPage - 1) / perPage
        val pageNum: Component = text(page, NamedTextColor.RED)
            .append(text(" of ", NamedTextColor.AQUA))
            .append(text(pages, NamedTextColor.RED))
        val header: Component =
            text("Recent Reports", NamedTextColor.GRAY)
                .append(text(" (").append(headerResultCount).append(text(") » ")).append(pageNum))
        val formattedHeader: Component = TextFormatter.horizontalLineHeading(sender, header, NamedTextColor.DARK_GRAY)
        object : PrettyPaginatedComponentResults<Report>(formattedHeader, 6) {
            override fun format(data: Report, index: Int): Component {
                val reporter: Component = text("Reported by ", NamedTextColor.GRAY)
                match?.let { reporter.append(data.getSenderComponent(it)) }
                val timeAgo: Component = TemporalComponent.relativePastApproximate(
                    Instant.ofEpochMilli(data.timeSent.toEpochMilli())
                )
                    .color(NamedTextColor.DARK_GREEN)
                return text()
                    .append(timeAgo.hoverEvent(showText(reporter)))
                    .append(text(": ", NamedTextColor.GRAY))
                    .append(if (match != null) data.getTargetComponent(match) else text(data.targetName, NamedTextColor.DARK_AQUA))
                    .append(text(" « ", NamedTextColor.YELLOW))
                    .append(text(data.reason, NamedTextColor.WHITE, TextDecoration.ITALIC))
                    .build()
            }

        }.display(audience, reportList, page)
    }

}
