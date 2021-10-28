package network.warzone.pgm

import app.ashcon.intake.bukkit.BukkitIntake
import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph
import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.commands.CommandModule
import network.warzone.pgm.feature.FeatureManager
import network.warzone.pgm.match.MatchManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class WarzonePGM : JavaPlugin() {

    companion object {
        lateinit var instance: WarzonePGM

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

