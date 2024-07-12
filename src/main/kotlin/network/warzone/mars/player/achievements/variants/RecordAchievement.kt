package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.Agent
import network.warzone.api.database.models.RecordType
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler
import java.util.*
import java.util.concurrent.TimeUnit

//TODO: This is currently experimental. I haven't done any tests on this achievement,
// nor do I know whether or not it works atm. There are probably some issues with the
// unit of time in each recordType too.
class RecordAchievement(
    val params: Agent.RecordAgentParams<Number>,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        val records = event.update.updated.stats.records
        val playerProfile = event.update.updated

        when (params.recordType) {
            RecordType.LONGEST_SESSION -> {
                val session = records.longestSession
                val elapsedTime = session?.let { differenceInMinutes(it.createdAt, it.endedAt) }
                if ((elapsedTime ?: 0L) >= params.threshold.toLong()) {
                    emitter.emit(event.update.updated.name)
                }
            }
            RecordType.LONGEST_PROJECTILE_KILL -> {
                val longestKill = records.longestProjectileKill?.distance ?: 0
                if (longestKill >= params.threshold.toInt()) {
                    emitter.emit(event.update.updated.name)
                }
            }
            RecordType.FASTEST_WOOL_CAPTURE -> {
                val woolCaptureTime = records.fastestWoolCapture?.value ?: Long.MAX_VALUE
                if (woolCaptureTime <= params.threshold.toLong()) {
                    emitter.emit(event.update.updated.name)
                }
            }
            RecordType.FASTEST_FLAG_CAPTURE -> {
                val flagCaptureTime = records.fastestFlagCapture?.value ?: Long.MAX_VALUE
                if (flagCaptureTime <= params.threshold.toLong()) {
                    emitter.emit(event.update.updated.name)
                }
            }
            RecordType.FASTEST_FIRST_BLOOD -> {
                val firstBloodTime = records.fastestFirstBlood?.time ?: Long.MAX_VALUE
                if (firstBloodTime <= params.threshold.toLong()) {
                    emitter.emit(event.update.updated.name)
                }
            }
            RecordType.KILLS_IN_MATCH -> {
                val kills = records.killsInMatch?.value ?: 0
                if (kills >= params.threshold.toInt()) {
                    emitter.emit(event.update.updated.name)
                }
            }
            RecordType.DEATHS_IN_MATCH -> {
                val deaths = records.deathsInMatch?.value ?: 0
                if (deaths >= params.threshold.toInt()) {
                    emitter.emit(event.update.updated.name)
                }
            }
        }
    }
}

private fun differenceInMinutes(start: Date, end: Date?): Long {
    if (end == null) return 0L

    val differenceInMillis = end.time - start.time
    return TimeUnit.MILLISECONDS.toMinutes(differenceInMillis)
}


