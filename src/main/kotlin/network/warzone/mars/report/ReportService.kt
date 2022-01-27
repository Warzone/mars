package network.warzone.mars.report

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.models.SimplePlayer

object ReportService {
    suspend fun create(target: SimplePlayer, reporter: SimplePlayer, reason: String, onlineStaff: Set<SimplePlayer>) {
        ApiClient.post<Unit, ReportCreateRequest>(
            "/mc/reports",
            ReportCreateRequest(target, reporter, reason, onlineStaff)
        )
    }

    data class ReportCreateRequest(
        val target: SimplePlayer,
        val reporter: SimplePlayer,
        val reason: String,
        val onlineStaff: Set<SimplePlayer>
    )
}