package network.warzone.mars.match.tracker

import kotlinx.coroutines.runBlocking
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PartyJoinData
import network.warzone.mars.api.socket.models.PartyLeaveData
import network.warzone.mars.api.socket.models.PlayerDeathData
import network.warzone.mars.match.MatchManager
import network.warzone.mars.match.deaths.LegacyTextDeathMessageBuilder
import network.warzone.mars.match.models.DeathCause
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.ProjectileRecord
import network.warzone.mars.utils.KEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.util.Vector
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerJoinPartyEvent
import tc.oc.pgm.events.PlayerLeavePartyEvent
import java.util.*
import kotlin.math.roundToInt

class PlayerTracker : Listener {

    // entity uuid mapped to player uuid & location
    val projectileCache = hashMapOf<UUID, Pair<UUID, Vector>>()

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

    @EventHandler
    fun onPlayerLevelUp(event: PlayerLevelUpEvent) = runBlocking {
        val player = Bukkit.getPlayer(event.data.playerId) ?: return@runBlocking
        player.sendMessage("${ChatColor.GREEN}${ChatColor.BOLD}Level up! ${ChatColor.GREEN}You are now level ${ChatColor.WHITE}${ChatColor.BOLD}${event.data.newLevel}${ChatColor.GREEN}!") // todo: make nicer message
        player.playSound(player.location, Sound.LEVEL_UP, 1000f, 1f)

        val context = PlayerManager.getPlayer(event.data.playerId)!!
        val profile = context.getPlayerProfile()
        profile.stats.xp = event.data.xp
        PlayerFeature.add(profile)
    }

    @EventHandler
    fun onProjectileShoot(event: ProjectileLaunchEvent) {
        if (event.actor !is Player) return
        projectileCache[event.entity.uniqueId] = Pair(event.actor.uniqueId, event.actor.location.toVector())
    }

    @EventHandler
    fun onProjectileHit(event: EntityDamageByEntityEvent) = runBlocking {
        event.damager as? Projectile
        event.entity as? Player
        println(event.entity.type)
        val info = projectileCache[event.damager.uniqueId] ?: return@runBlocking
        val shooter = PlayerFeature.getOrNull(info.first) ?: return@runBlocking

        val originalLocation = info.second
        val newLocation = event.entity.location.toVector()

        val distance = originalLocation.distance(newLocation).roundToInt()

        if (distance <= (shooter.stats.records.longestProjectileHit?.distance ?: 0)) return@runBlocking // doesn't beat record

        projectileCache.remove(event.damager.uniqueId)
//        shooter.stats.records.longestProjectileHit = ProjectileRecord(MatchManager.match.id, event.damager.type, distance)
        ApiClient.emit(OutboundEvent.ProjectileHit, ProjectileHitData(event.actor.uniqueId, event.damager.type, distance))
    }
}

data class PlayerLevelUpEvent(val data: PlayerLevelUpData) : KEvent()
data class PlayerLevelUpData(val playerId: UUID, val newLevel: Int, val xp: Int)

data class ProjectileHitData(val playerId: UUID, val type: EntityType, val distance: Int)