package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.match.deaths.LegacyTextDeathMessageBuilder
import network.warzone.pgm.match.models.DeathCause
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerJoinPartyEvent
import tc.oc.pgm.events.PlayerLeavePartyEvent
import java.util.*

class PlayerTracker : Listener {

    data class PartyJoinData(val playerId: UUID, val playerName: String, val partyName: String)
    data class PartyLeaveData(val playerId: UUID, val playerName: String)
    data class PlayerDeathData(
        val victimId: UUID,
        val victimName: String,
        val attackerId: UUID?,
        val attackerName: String?,
        val weapon: String?,
        val entity: String?,
        val distance: Long?,
        val key: String,
        val cause: DeathCause
    )

    object PartyJoin : OutboundEvent<PartyJoinData>("PARTY_JOIN")
    object PartyLeave : OutboundEvent<PartyLeaveData>("PARTY_LEAVE")
    object PlayerDeath : OutboundEvent<PlayerDeathData>("PLAYER_DEATH")

    @EventHandler
    fun onPartyJoin(event: PlayerJoinPartyEvent) {
        if (event.newParty !is Competitor || !event.match.isRunning) return // Joining spectator

        val party: Party = event.newParty!!

        ApiClient.emit(
            PartyJoin,
            PartyJoinData(event.player.id, event.player.nameLegacy, party.defaultName)
        )
    }

    @EventHandler
    fun onPartyLeave(event: PlayerLeavePartyEvent) {
        if (event.party !is Competitor || !event.match.isRunning) return // Leaving spectator

        ApiClient.emit(
            PartyLeave,
            PartyLeaveData(event.player.id, event.player.nameLegacy)
        )
    }

    @EventHandler
    fun onPlayerDeath(event: MatchPlayerDeathEvent) {
        val translatableComponent = LegacyTextDeathMessageBuilder(event)

        val weapon = translatableComponent.weaponString
        val mob = translatableComponent.mobString
        val distance = translatableComponent.distance

        ApiClient.emit(
            PlayerDeath,
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