package network.warzone.mars.broadcast

import network.warzone.mars.Mars
import network.warzone.mars.feature.Feature
import network.warzone.mars.utils.color
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

object BroadcastFeature : Feature<Broadcast>() {
    private var broadcasts = listOf<Broadcast>()

    private var task: BukkitTask? = null
    private var index = 0

    private const val BROADCAST_INTERVAL = 20 * 60L // 20 ticks = 1 second, so interval is 1 minute

    var autoBroadcast: Boolean = true
        set(value) {
            field = value
            stopTask()
            if (value) startTask()
        }

    override suspend fun fetch(target: String): Broadcast? {
        return broadcasts.find { it.name.equals(target, ignoreCase = true) }
    }

    override fun fetchCached(target: String): Broadcast? {
        return broadcasts.find { it.name.equals(target, ignoreCase = true) }
    }

    override suspend fun init() {
        index = 0
        broadcasts = BroadcastService.getBroadcasts()
        autoBroadcast = true
        Bukkit.getLogger().info("Loaded ${broadcasts.size} broadcasts from API")
        startTask()
    }

    private fun startTask() {
        stopTask()
        if (broadcasts.size > 0)
            task = Bukkit.getScheduler().runTaskTimer(Mars.get(), {
                if ((index + 1) > broadcasts.size) index = 0
                val broadcast = broadcasts[index++]
                broadcast(broadcast.message, broadcast.newline, broadcast.permission)
            }, BROADCAST_INTERVAL, BROADCAST_INTERVAL)
    }

    private fun stopTask() {
        val taskId = task?.taskId ?: return
        Bukkit.getScheduler().cancelTask(taskId)
        task = null
    }

    fun broadcast(message: String, newline: Boolean = true, permission: String?) {
        var formatted = message.replace("\\n", "\n")
        val nl = if (newline) "\n" else ""
        formatted = "$nl&7$formatted$nl ".color()

        if (permission != null) Bukkit.getOnlinePlayers().filter { it.hasPermission(permission) }.forEach { it.sendMessage(formatted) }
        else Bukkit.getOnlinePlayers().forEach { it.sendMessage(formatted) }
    }

    suspend fun reload() {
        stopTask()
        init()
    }

    override fun getSubcommands(): Map<List<String>, Any> {
        return mapOf(listOf("broadcasts", "broadcast", "bc") to BroadcastCommands())
    }

    /**
     * Returns a copy of the broadcasts list.
      */
    fun getBroadcasts(): List<Broadcast> {
        return broadcasts.toList()
    }
}