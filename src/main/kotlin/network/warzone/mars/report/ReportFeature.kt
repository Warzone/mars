package network.warzone.mars.report

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.Feature
import network.warzone.mars.feature.Resource
import network.warzone.mars.utils.simple
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.community.events.PlayerReportEvent
import java.util.*

object ReportFeature : Feature<Report>(), Listener {
    init {
        Mars.registerEvents(this)
    }

    override suspend fun fetch(target: String): Report? {
        TODO("Reports are not fetchable")
    }

    @EventHandler
    fun onReportCreate(event: PlayerReportEvent) = runBlocking {
        val onlineStaff = Bukkit.getOnlinePlayers().filter { it.hasPermission("pgm.staff") }
            .map { SimplePlayer(it.uniqueId, it.name) }.toSet()

        ReportService.create(event.player.simple, event.sender.simple, event.reason, onlineStaff)
    }
}

// Not a real construct
data class Report(override val _id: UUID = UUID.randomUUID()) : Resource