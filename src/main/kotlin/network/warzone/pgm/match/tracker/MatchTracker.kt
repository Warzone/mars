package network.warzone.pgm.match.tracker

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.api.socket.models.*
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

class MatchTracker : Listener {

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) = runBlocking {
        val parties: List<PartyData> = event.match.parties
            .filterIsInstance<Team>()
            .map { PartyData(it.defaultName, it.nameLegacy, it.color.name, it.minPlayers, it.maxPlayers) }

        val gameMap: GameMap = MapFeature.getKnown(event.match.map.name)

        val match = event.match

        ApiClient.emit(OutboundEvent.MatchLoad, MatchLoadData(
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
        ApiClient.emit(
            OutboundEvent.MatchEnd,
            MatchEndData(event.match.winners.toMutableList()[0]?.nameLegacy)
        )
    }

    private fun getFlagPartials(match: Match):  List<FlagPartial> {
        return match.getModule(FlagMatchModule::class.java)
            ?.flags
            ?.map { FlagPartial(it.id, it.name, it.bukkitColor, it.owner?.nameLegacy) } ?: listOf()
    }

    private fun getWoolPartials(match: Match): List<WoolPartial> {
        return match.getModule(WoolMatchModule::class.java)
            ?.wools
            ?.values()
            ?.map {
                WoolPartial(
                    it.id,
                    it.name,
                    it.owner?.nameLegacy,
                    BukkitUtils.dyeColorToChatColor(it.dyeColor)
                )
            } ?: listOf()
    }

    private fun getCorePartials(match: Match): List<CorePartial> {
        return match.getModule(GoalMatchModule::class.java)
            ?.getGoals(Core::class.java)
            ?.values()
            ?.map { CorePartial(it.id, it.name, it.owner.nameLegacy, it.material.itemType) } ?: listOf()
    }

    private fun getDestroyablePartial(match: Match): List<DestroyablePartial> {
        return match.getModule(DestroyableMatchModule::class.java)
            ?.destroyables
            ?.map { DestroyablePartial(
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

    private fun getControlPointPartial(match: Match): List<ControlPointPartial> {
        return match.getModule(GoalMatchModule::class.java)
            ?.getGoals(ControlPoint::class.java)
            ?.values()
            ?.map { ControlPointPartial(it.id, it.name) } ?: listOf()
    }
}