package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PartyJoinData
import network.warzone.mars.api.socket.models.PartyLeaveData
import network.warzone.mars.api.socket.models.PlayerDeathData
import network.warzone.mars.match.deaths.LegacyTextDeathMessageBuilder
import network.warzone.mars.match.models.DeathCause
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerJoinPartyEvent
import tc.oc.pgm.events.PlayerLeavePartyEvent

class PlayerTracker : Listener {

    @EventHandler
    fun onPartyJoin(event: PlayerJoinPartyEvent) {
        if (event.newParty !is Competitor || !event.match.isRunning) return // Joining spectator

        val party: Party = event.newParty!!

        ApiClient.emit(
            OutboundEvent.PartyJoin,
            PartyJoinData(event.player.id, event.player.nameLegacy, party.defaultName)
        )
    }

    @EventHandler
    fun onPartyLeave(event: PlayerLeavePartyEvent) {
        if (event.party !is Competitor || !event.match.isRunning) return // Leaving spectator

        ApiClient.emit(
            OutboundEvent.PartyLeave,
            PartyLeaveData(event.player.id, event.player.nameLegacy)
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: MatchPlayerDeathEvent) {
        val translatableComponent = LegacyTextDeathMessageBuilder(event)

        val weapon = translatableComponent.weaponString
        val mob = translatableComponent.mobString
        val distance = translatableComponent.distance

        ApiClient.emit(
            OutboundEvent.PlayerDeath,
            PlayerDeathData(
                victimId = event.victim.id,
                victimName = event.victim.nameLegacy,
                attackerId = event.killer?.id,
                attackerName = event.killer?.nameLegacy,
                weapon = weapon,
                entity = mob,
                distance = distance,
                key = translatableComponent.key!!,
                cause = DeathCause.fromDamageInfo(event.damageInfo)
            )
        )
    }

}
