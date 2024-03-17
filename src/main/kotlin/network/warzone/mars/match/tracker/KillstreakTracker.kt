package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.KillstreakData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.match.MatchManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import tc.oc.pgm.api.player.ParticipantState
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import tc.oc.pgm.util.named.NameStyle
import java.util.*

object KillstreakTracker : Listener {
    private val trackedKillstreaks = mapOf(
        5 to NamedTextColor.DARK_GREEN,
        10 to NamedTextColor.GOLD,
        25 to NamedTextColor.RED,
        50 to NamedTextColor.DARK_AQUA,
        100 to NamedTextColor.DARK_PURPLE,
        120 to NamedTextColor.BLUE,
        150 to NamedTextColor.YELLOW,
        200 to NamedTextColor.BLACK,
    )

    fun getKillstreak(id: UUID): Int? {
        val statsModule = MatchManager.getTracker(BigStatsTracker::class)?.matchStatsModule ?: return null
        val stats = statsModule.getPlayerStat(id)
        return stats.killstreak
    }

    fun getNearestTrackedKillstreak(killstreak: Int): Pair<Int, NamedTextColor> {
        if (killstreak <= 5) return Pair(5, trackedKillstreaks[5]!!)
        var nearestKillstreak = killstreak
        while (trackedKillstreaks[nearestKillstreak] == null) {
            nearestKillstreak--
        }
        return Pair(nearestKillstreak, trackedKillstreaks[nearestKillstreak]!!)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onKillstreak(event: MatchPlayerDeathEvent) {
        val killer = event.killer ?: return
        if (!event.isEnemyKill) return
        val killstreak = getKillstreak(killer.id)!! + 1
        if (!trackedKillstreaks.contains(killstreak)) return
        event.match.sendMessage(createKillstreakMessage(killer, killstreak))
        ApiClient.emit(
            OutboundEvent.Killstreak,
            KillstreakData(killstreak, SimplePlayer(killer.id, killer.nameLegacy), false)
        )
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onKillstreakEnd(event: MatchPlayerDeathEvent) {
        val victim = event.victim
        val killer = event.killer ?: return // no killer = no broadcast
        val killstreak = getKillstreak(victim.id)!!

        if (killstreak >= 5) {
            event.match.sendMessage(createKillstreakEndMessage(killer, event.victim.participantState!!, killstreak))

            val nearestKillstreak = getNearestTrackedKillstreak(killstreak).first
            ApiClient.emit(
                OutboundEvent.Killstreak,
                KillstreakData(nearestKillstreak, SimplePlayer(killer.id, killer.nameLegacy), true)
            )
        }
    }

    private fun createKillstreakMessage(killer: ParticipantState, count: Int): ComponentLike {
        val killerComp = killer.getName(NameStyle.COLOR)
        val countComp = Component.text(count, trackedKillstreaks[count]).decorate(TextDecoration.BOLD)
        return Component.text()
                .append(killerComp)
                .append(Component.text(" is on a killstreak of ", NamedTextColor.GRAY))
                .append(countComp)
                .append(Component.text("!", NamedTextColor.GRAY))
    }

    private fun createKillstreakEndMessage(ender: ParticipantState, ended: ParticipantState, count: Int): ComponentLike {
        val enderComp = ender.getName(NameStyle.COLOR)
        val endedComp = ended.getName(NameStyle.COLOR)
        val nearestColor = getNearestTrackedKillstreak(count).second
        val countComp = Component.text(count, nearestColor).decorate(TextDecoration.BOLD)
        return Component.text()
            .append(enderComp)
            .append(Component.text(" ended ", NamedTextColor.GRAY))
            .append(endedComp)
            .append(Component.text("'s killstreak of ", NamedTextColor.GRAY))
            .append(countComp)
    }
}