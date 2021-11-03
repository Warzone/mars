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
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.controlpoint.ControlPoint
import tc.oc.pgm.core.Core
import tc.oc.pgm.destroyable.DestroyableMatchModule
import tc.oc.pgm.flag.FlagMatchModule
import tc.oc.pgm.goals.GoalMatchModule
import tc.oc.pgm.stats.StatsMatchModule
import tc.oc.pgm.teams.Team
import tc.oc.pgm.util.bukkit.BukkitUtils
import tc.oc.pgm.wool.WoolMatchModule
import java.util.*

class MatchTracker : Listener {

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) = runBlocking {
        val parties: List<PartyData> = event.match.parties
            .filterIsInstance<Team>()
            .map { PartyData(it.defaultName, it.nameLegacy, it.color.name, it.minPlayers, it.maxPlayers) }

        val gameMap: GameMap = MapFeature.getKnown(event.match.map.name)

        val match = event.match

        ApiClient.emit(
            OutboundEvent.MatchLoad, MatchLoadData(
                gameMap._id,
                parties,
                MatchLoadGoals(
                    flags = getFlagPartials(match),
                    wools = getWoolPartials(match),
                    cores = getCorePartials(match),
                    controlPoints = getControlPointPartial(match),
                    destroyables = getDestroyablePartial(match)
                )
            )
        )

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
        val bigStatsTracker = MatchManager.getTracker(BigStatsTracker::class) ?: return
        val playerStatsMap: MutableMap<UUID, BigStats> = mutableMapOf()
        val statsModule = event.match.getModule(StatsMatchModule::class.java)
        if (statsModule != null) {
            event.match.players.map { it.id }.plus(bigStatsTracker.offlinePlayersPendingStatSave).distinct().forEach {
                val stats = statsModule.getPlayerStat(it)
                val blocks = bigStatsTracker.blockCache[it]
                val messages = bigStatsTracker.messageCache[it]
                val playerBigStats = BigStats(
                    blocks,
                    messages = messages ?: PlayerMessages(),
                    bowShotsTaken = stats.shotsTaken,
                    bowShotsHit = stats.shotsHit,
                    damageGiven = stats.damageDone,
                    damageTaken = stats.damageTaken,
                    damageGivenBow = stats.bowDamage
                )
                if (!playerBigStats.isDefault()) playerStatsMap[it] = playerBigStats
            }
        }

        ApiClient.emit(
            OutboundEvent.MatchEnd,
            MatchEndData(event.match.winners.map { it.defaultName }, bigStats = playerStatsMap)
        )
    }

    @EventHandler
    fun onWoolDrop(event: MatchPlayerDeathEvent) {
        println("Wool drop event in MatchTracker")
        val tracker = MatchManager.getTracker(WoolTracker::class) ?: return
        println("Tracker is here")
        tracker.holdingCache.forEach {
            Bukkit.broadcastMessage("${it.key} ${it.value}")
        }
    }

    private fun getFlagPartials(match: Match): List<FlagPartial> {
        return match.getModule(FlagMatchModule::class.java)
            ?.flags
            ?.map { FlagPartial(it.id, it.name, it.bukkitColor, it.owner?.nameLegacy) }?.distinctBy { it.id }
            ?: listOf()
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
            }?.distinctBy { it.id } ?: listOf()
    }

    private fun getCorePartials(match: Match): List<CorePartial> {
        return match.getModule(GoalMatchModule::class.java)
            ?.getGoals(Core::class.java)
            ?.values()
            ?.map { CorePartial(it.id, it.name, it.owner.nameLegacy, it.material.itemType) }?.distinctBy { it.id }
            ?: listOf()
    }

    private fun getDestroyablePartial(match: Match): List<DestroyablePartial> {
        return match.getModule(DestroyableMatchModule::class.java)
            ?.destroyables
            ?.map {
                DestroyablePartial(
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
                )
            }?.distinctBy { it.id } ?: listOf()
    }

    private fun getControlPointPartial(match: Match): List<ControlPointPartial> {
        return match.getModule(GoalMatchModule::class.java)
            ?.getGoals(ControlPoint::class.java)
            ?.values()
            ?.map { ControlPointPartial(it.id, it.name) }?.distinctBy { it.id } ?: listOf()
    }
}