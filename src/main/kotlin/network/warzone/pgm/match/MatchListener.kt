package network.warzone.pgm.match

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.api.socket.*
import network.warzone.pgm.map.MapFeature
import network.warzone.pgm.map.models.GameMap
import network.warzone.pgm.match.deaths.LegacyTextDeathMessageBuilder
import network.warzone.pgm.match.models.DeathCause
import network.warzone.pgm.match.models.LiveMatchPlayer
import network.warzone.pgm.match.models.PartyData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.core.CoreLeakEvent
import tc.oc.pgm.events.PlayerJoinPartyEvent
import tc.oc.pgm.events.PlayerLeavePartyEvent
import tc.oc.pgm.flag.event.FlagCaptureEvent
import tc.oc.pgm.teams.Team
import tc.oc.pgm.wool.PlayerWoolPlaceEvent

class MatchListener : Listener {

    private val apiClient get() = WarzonePGM.get().apiClient

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) = runBlocking {
        val parties: List<PartyData> = event.match.parties
            .filterIsInstance<Team>()
            .map { PartyData(it.defaultName, it.nameLegacy, it.color.name, it.minPlayers, it.maxPlayers) }

        val gameMap: GameMap = MapFeature.getKnown(event.match.map.name)

        apiClient.emit(
            OutboundEvent.MatchLoad,
            MatchLoadData(gameMap._id, parties)
        )
    }

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        apiClient.emit(
            OutboundEvent.MatchStart,
            MatchStartData(
                participants = event.match
                    .participants
                    .map { LiveMatchPlayer(it.nameLegacy, it.id, it.party.nameLegacy) }
                    .toSet()
            )
        )
    }

    @EventHandler
    fun onMatchEnd(event: MatchFinishEvent) {
        apiClient.emit(
            OutboundEvent.MatchEnd,
            Unit
        )
    }

    @EventHandler
    fun onPartyJoin(event: PlayerJoinPartyEvent) {
        if (event.newParty !is Competitor || !event.match.isRunning) return // Joining spectator

        val party: Party = event.newParty!!

        apiClient.emit(
            OutboundEvent.PartyJoin,
            PartyJoinData(event.player.id, event.player.nameLegacy, party.defaultName)
        )
    }

    @EventHandler
    fun onPartyLeave(event: PlayerLeavePartyEvent) {
        if (event.party !is Competitor || !event.match.isRunning) return // Leaving spectator

        apiClient.emit(
            OutboundEvent.PartyLeave,
            PartyLeaveData(event.player.id, event.player.nameLegacy)
        )
    }

    @EventHandler
    fun onPlayerDeath(event: MatchPlayerDeathEvent) {
        val translatableComponent = LegacyTextDeathMessageBuilder(event)

        val weapon = translatableComponent.weaponString
        val mob = translatableComponent.mobString
        val distance = translatableComponent.distance

        apiClient.emit(
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

    @EventHandler
    fun onObjectiveCoreLeak(event: CoreLeakEvent) {}

    @EventHandler
    fun onObjectiveFlagCapture(event: FlagCaptureEvent) {}

    @EventHandler
    fun onObjectiveWoolPlace(event: PlayerWoolPlaceEvent) {}

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {}

}