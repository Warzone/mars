package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.KillStreakData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.match.MatchManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import tc.oc.pgm.api.player.ParticipantState
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.lib.net.kyori.adventure.key.Key
import tc.oc.pgm.lib.net.kyori.adventure.sound.Sound
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import tc.oc.pgm.util.named.NameStyle

class KillstreakTracker : Listener {
    val trackedKillstreaks = mapOf(
        5 to NamedTextColor.DARK_GREEN,
        10 to NamedTextColor.GOLD,
        25 to NamedTextColor.RED,
        50 to NamedTextColor.DARK_AQUA,
        100 to NamedTextColor.DARK_PURPLE
    )

    @EventHandler(priority = EventPriority.HIGH)
    fun onKillstreak(event: MatchPlayerDeathEvent) {
        val killer = event.killer ?: return
        if (!event.isEnemyKill) return
        val statsModule = MatchManager.getTracker(BigStatsTracker::class)?.matchStatsModule ?: return
        val stats = statsModule.getPlayerStat(killer)
        if (!trackedKillstreaks.contains(stats.killstreak)) return
        event.match.sendMessage(
            createKillstreakMessage(killer, stats.killstreak)
        )
        ApiClient.emit(
            OutboundEvent.Killstreak,
            KillStreakData(stats.killstreak, SimplePlayer(killer.id, killer.nameLegacy), false)
        )
    }

    private fun createKillstreakMessage(killer: ParticipantState, count: Int): Component {
        val killerComp = killer.getName(NameStyle.COLOR).decorate(TextDecoration.BOLD)
        val countComp =
            Component.text(count, trackedKillstreaks[count]).decorate(TextDecoration.BOLD)
        return killerComp.append(Component.text(" is on a kill streak of ", NamedTextColor.GRAY)).append(countComp)
            .append(Component.text("!", NamedTextColor.GRAY))
    }
}