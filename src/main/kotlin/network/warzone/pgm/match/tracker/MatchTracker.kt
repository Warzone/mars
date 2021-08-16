package network.warzone.pgm.match.tracker

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.map.MapFeature
import network.warzone.pgm.map.models.GameMap
import network.warzone.pgm.match.MatchManager
import network.warzone.pgm.match.models.LiveMatchPlayer
import network.warzone.pgm.match.models.PartyData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.controlpoint.ControlPoint
import tc.oc.pgm.core.Core
import tc.oc.pgm.destroyable.DestroyableMatchModule
import tc.oc.pgm.flag.FlagMatchModule
import tc.oc.pgm.goals.GoalMatchModule
import tc.oc.pgm.teams.Team
import tc.oc.pgm.util.bukkit.BukkitUtils
import tc.oc.pgm.wool.WoolMatchModule
import java.util.*

class MatchTracker : Listener {

    data class MatchLoadGoals(
        val flags: List<FlagTracker.FlagPartial>,
        val wools: List<WoolTracker.WoolPartial>,
        val cores: List<CoreTracker.CorePartial>,
        val controlPoints: List<ControlPointTracker.ControlPointPartial>,
        val destroyables: List<DestroyableTracker.DestroyablePartial>
    )

    data class MatchLoadData(
        val mapId: UUID,
        val parties: List<PartyData>,
        val goals: MatchLoadGoals
        )
    data class MatchStartData(val participants: Set<LiveMatchPlayer>)
    data class MatchEndData(val winningTeam: String)

    object MatchLoad : OutboundEvent<MatchLoadData>("MATCH_LOAD")
    object MatchStart : OutboundEvent<MatchStartData>("MATCH_START")
    object MatchEnd : OutboundEvent<Unit>("MATCH_END")

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) = runBlocking {
        val parties: List<PartyData> = event.match.parties
            .filterIsInstance<Team>()
            .map { PartyData(it.defaultName, it.nameLegacy, it.color.name, it.minPlayers, it.maxPlayers) }

        val gameMap: GameMap = MapFeature.getKnown(event.match.map.name)

        val match = event.match

        ApiClient.emit(MatchLoad, MatchLoadData(
            gameMap._id,
            parties,
            MatchLoadGoals(
                flags = getFlagPartials(match),
                wools = getWoolPartials(match),
                cores = getCorePartials(match),
                controlPoints = getControlPointPartial(match),
                destroyables = getDestroyablePartial(match)
            )
        ))

        MatchManager.match = event.match
    }

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        ApiClient.emit(
            MatchStart,
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
        ApiClient.emit(
            MatchEnd,
            Unit
        )
    }

    private fun getFlagPartials(match: Match):  List<FlagTracker.FlagPartial> {
        return match.getModule(FlagMatchModule::class.java)
            ?.flags
            ?.map { FlagTracker.FlagPartial(it.id, it.name, it.bukkitColor, it.owner?.nameLegacy) } ?: listOf()
    }

    private fun getWoolPartials(match: Match): List<WoolTracker.WoolPartial> {
        return match.getModule(WoolMatchModule::class.java)
            ?.wools
            ?.values()
            ?.map { WoolTracker.WoolPartial(
                it.id,
                it.name,
                it.owner!!.nameLegacy,
                BukkitUtils.dyeColorToChatColor(it.dyeColor
                )
            ) } ?: listOf()
    }

    private fun getCorePartials(match: Match): List<CoreTracker.CorePartial> {
        return match.getModule(GoalMatchModule::class.java)
            ?.getGoals(Core::class.java)
            ?.values()
            ?.map { CoreTracker.CorePartial(it.id, it.name, it.owner.nameLegacy, it.material.itemType) } ?: listOf()
    }

    private fun getDestroyablePartial(match: Match): List<DestroyableTracker.DestroyablePartial> {
        return match.getModule(DestroyableMatchModule::class.java)
            ?.destroyables
            ?.map { DestroyableTracker.DestroyablePartial(
                it.id,
                it.name,
                it.owner.nameLegacy,
                it.blockRegion.blocks
                    .map { block -> block.type }
                    .groupingBy { self -> self }
                    .eachCount()
                    .maxByOrNull { group -> group.value }
                !!.key,
                it.blockRegion.blocks.size
            ) } ?: listOf()
    }

    private fun getControlPointPartial(match: Match): List<ControlPointTracker.ControlPointPartial> {
        return match.getModule(GoalMatchModule::class.java)
            ?.getGoals(ControlPoint::class.java)
            ?.values()
            ?.map { ControlPointTracker.ControlPointPartial(it.id, it.name) } ?: listOf()
    }
}