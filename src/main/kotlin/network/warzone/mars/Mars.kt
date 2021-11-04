package network.warzone.mars

import app.ashcon.intake.bukkit.BukkitIntake
import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph
import network.warzone.mars.api.ApiClient
import network.warzone.mars.commands.CommandModule
import network.warzone.mars.feature.FeatureManager
import network.warzone.mars.match.MatchManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Mars : JavaPlugin() {

    companion object {
        lateinit var instance: Mars

        fun get() = instance

        fun registerEvents(listener: Listener) = Bukkit.getPluginManager().registerEvents(listener, instance)
    }

    lateinit var serverId: String

    override fun onEnable() {
        println("enabling!")
        instance = this

        serverId = config.getString("server.id")

        val commandGraph = BasicBukkitCommandGraph(CommandModule)

        FeatureManager.init()
        FeatureManager.registerCommands(commandGraph)

        val apiConfigurationSection = config.getConfigurationSection("api")
        ApiClient.loadHttp(apiConfigurationSection)
        ApiClient.loadSocket(serverId, apiConfigurationSection)

        MatchManager.init()

        BukkitIntake(this, commandGraph).register()
    }

    override fun onDisable() {
        println("hello!")
    }

}

