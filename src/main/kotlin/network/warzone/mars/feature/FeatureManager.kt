package network.warzone.mars.feature

import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph
import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.events.ApiConnectedEvent
import network.warzone.mars.feature.resource.ResourceType
import network.warzone.mars.map.MapFeature
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.punishment.PunishmentFeature
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.tag.TagFeature
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object FeatureManager : Listener {

    private val features: MutableMap<ResourceType<*>, Feature<*, *>> = mutableMapOf(
        ResourceType.Player to PlayerFeature,
        ResourceType.Rank to RankFeature,
        ResourceType.Tag to TagFeature,
        ResourceType.Map to MapFeature,
        ResourceType.Punishment to PunishmentFeature
    )

    fun init() {
        Mars.registerEvents(this)
    }

    fun <T : Feature<*, *>> getFeature(type: ResourceType<T>): T {
        return type.cast(features[type]!!)
    }

    fun registerCommands(graph: BasicBukkitCommandGraph) {
        features.values.forEach {
            it.getSubcommands().forEach { entry ->
                val node = graph.rootDispatcherNode.registerNode(*entry.key.toTypedArray())
                node.registerCommands(entry.value)
            }

            it.getCommands().forEach { command ->
                graph.rootDispatcherNode.registerCommands(command)
            }
        }
    }

    @EventHandler
    fun onApiConnected(event: ApiConnectedEvent) = runBlocking {
        println("API connected event called.")
        features.values.forEach {
            it.init()
        }
    }

}