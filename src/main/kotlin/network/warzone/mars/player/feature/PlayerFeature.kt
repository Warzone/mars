package network.warzone.mars.player.feature

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.admin.AdminCommands
import network.warzone.mars.admin.AdminService
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.NamedCachedFeature
import network.warzone.mars.leaderboard.LeaderboardCommands
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.commands.ChatCommands
import network.warzone.mars.player.commands.MiscCommands
import network.warzone.mars.player.commands.ModCommands
import network.warzone.mars.player.commands.StatCommands
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.player.models.Session
import network.warzone.mars.player.perks.JoinSoundService
import network.warzone.mars.player.perks.PerkCommands
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.RankAttachments
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.KEvent
import network.warzone.mars.utils.color
import network.warzone.mars.utils.getRelativeTime
import network.warzone.mars.utils.matchPlayer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import tc.oc.pgm.api.Permissions
import tc.oc.pgm.api.integration.Integration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class QueuedJoin(
    val isNew: Boolean,
    val profile: PlayerProfile,
    val activePunishments: List<Punishment>,
    val activeSession: Session
)

object PlayerFeature : NamedCachedFeature<PlayerProfile>(), Listener {
    private val queuedJoins = ConcurrentHashMap<UUID, QueuedJoin>()

    init {
        Mars.registerEvents(this)
    }

    override suspend fun fetch(target: String): PlayerProfile? {
        return try {
            ApiClient.get<PlayerProfile>("/mc/players/$target")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addRank(player: String, rank: Rank): PlayerProfile {
        val profile = PlayerService.addRank(player, rank)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
            RankAttachments.refresh(it)
        }

        return profile
    }

    suspend fun removeRank(player: String, rank: Rank): PlayerProfile {
        val profile = PlayerService.removeRank(player, rank)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
            RankAttachments.refresh(it)
        }

        return profile
    }

    suspend fun addTag(player: String, tag: String) {
        val profile = PlayerService.addTag(player, tag)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun removeTag(player: String, tag: String) {
        val profile = PlayerService.removeTag(player, tag)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun setActiveTag(player: String, tag: Tag?) {
        val profile = PlayerService.setActiveTag(player, tag)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun addNote(player: String, content: String, author: SimplePlayer) {
        val profile = PlayerService.addNote(player, content, author)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun removeNote(player: String, id: Int) {
        val profile = PlayerService.removeNote(player, id)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun getPunishmentHistory(player: String): List<Punishment> {
        return PlayerService.getPunishmentHistory(player)
    }

    suspend fun lookup(player: String, includeAlts: Boolean = false): PlayerService.PlayerLookupResponse {
        return PlayerService.lookup(player, includeAlts).also { add(it.player) }
    }

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) = runBlocking {
        try {
            val ip = event.address.hostAddress
            val (isNew, isAllowed, profile, activePunishments) = PlayerService.preLogin(
                event.uniqueId,
                event.name,
                ip
            )
            if (!isAllowed) { // Player is not allowed to join (banned)
                event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                val ban = activePunishments.find { it.action.isBan() }
                if (ban != null) {
                    Mars.get().logger.info("Player: ${event.uniqueId} (${event.name}) - Punishment ID: ${ban._id}")
                    val expiryString =
                        if (ban.action.isPermanent()) "&7This ban is permanent." else "&7This ban will expire &f${ban.expiresAt.getRelativeTime(precise=true)} (${ban.expiresAt})&7."
                    val appealLink = Mars.get().config.getString("server.links.appeal")
                        ?: throw RuntimeException("No appeal link set in config")
                    event.kickMessage =
                        "&7You have been ${ban.action.kind.pastTense} from the server for &c${ban.reason.name}&7.\n\n&c${ban.reason.message}\n\n$expiryString\n&7Appeal at &b$appealLink".color()
                } else {
                    event.kickMessage =
                        "&cYou are not allowed to join. Please contact staff or try again later.".color()
                }
            } else { // Join success
                val (activeSession) = PlayerService.login(event.uniqueId, event.name, ip)
                val queuedJoin = QueuedJoin(isNew, profile, activePunishments, activeSession)
                queuedJoins[event.uniqueId] = queuedJoin
            }
        } catch (e: Exception) {
            event.kickMessage = "&cUnable to load player profile. Please try again later or contact staff.".color()
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.player

        val (_, profile, activePuns, activeSession) = queuedJoins[player.uniqueId]
            ?: throw RuntimeException("Queued join unavailable: ${player.name} (${player.uniqueId})")

        val context = PlayerManager.createPlayer(player, activeSession, activePuns)

        add(profile)

        RankAttachments.createAttachment(context)
        Mars.async { RankAttachments.refresh(context) }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player.matchPlayer
        val join = queuedJoins[player.id] ?: return
        if (!Integration.isVanished(player.bukkit)) {
            Bukkit.broadcastMessage("${ChatColor.GRAY}${event.player.name} joined. ${if (join.isNew) "${ChatColor.LIGHT_PURPLE}[NEW]" else ""}")
            val joinSoundId = join.profile.activeJoinSoundId
            if (joinSoundId != null) {
                val sound = JoinSoundService.getSoundById(joinSoundId)
                if (sound != null && event.player.hasPermission(sound.permission)) {
                    JoinSoundService.playSound(sound)
                }
            }
        } else
            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission(Permissions.ADMINCHAT) }
                .forEach {
                    it.sendMessage("${ChatColor.GRAY}${ChatColor.ITALIC}${event.player.name} joined quietly.")
                }

        val xpMultiplier = AdminService.getCurrentEvents()?.xpMultiplier
        if (xpMultiplier?.value != null) {
            player.bukkit.sendMessage("${ChatColor.AQUA}${ChatColor.BOLD}✧ ${ChatColor.AQUA}XP Multiplier Active: ${ChatColor.WHITE}${ChatColor.BOLD}${xpMultiplier.value}x")
        }

        // Join process has finished, we don't need the queued join anymore
        queuedJoins.remove(player.id)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (!Integration.isVanished(event.player))
            Bukkit.broadcastMessage("${ChatColor.GRAY}${event.player.name} left.")
        else
            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission(Permissions.ADMINCHAT) }
                .forEach {
                    it.sendMessage("${ChatColor.GRAY}${ChatColor.ITALIC}${event.player.name} left quietly.")
                }
    }

    @EventHandler
    fun onPlayerLogout(event: PlayerQuitEvent) {
        Mars.async {
            val player = event.player
            val uuid = player.uniqueId

            val activeSession = PlayerManager.getPlayer(uuid)?.activeSession ?: return@async

            val context = PlayerManager.removePlayer(uuid)!! // We know the player is online
            RankAttachments.removeAttachment(context)

            remove(uuid)

            val sessionLength = Date().time - activeSession.createdAt.time
            PlayerService.logout(uuid, player.name, activeSession._id, sessionLength)
        }
    }

    // API is telling us to kick the player
    @EventHandler
    fun onDisconnectPlayer(event: DisconnectPlayerEvent) {
        val context = PlayerManager.getPlayer(event.data.playerId) ?: return
        val reason = event.data.reason?.color() ?: "Disconnected"
        Bukkit.getScheduler().runTask(Mars.get(), Runnable {
            context.player.kickPlayer("${ChatColor.RED}$reason")
        })
    }

    override fun getCommands(): List<Any> {
        return listOf(ModCommands(), MiscCommands(), StatCommands(), PerkCommands(), AdminCommands(), LeaderboardCommands())
    }

    override fun getSubcommands(): Map<List<String>, Any> {
        return mapOf(listOf("chat") to ChatCommands())
    }
}

data class DisconnectPlayerEvent(val data: DisconnectPlayerData) : KEvent()
data class DisconnectPlayerData(val playerId: UUID, val reason: String?)
