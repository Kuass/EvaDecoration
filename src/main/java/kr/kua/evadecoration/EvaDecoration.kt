package kr.kua.evadecoration

import kr.kua.evadecoration.entity.global.*
import kr.kua.evadecoration.gui.GuiManager
import kr.kua.evadecoration.service.EvaDecorationDAO
import kr.kua.evadecoration.listener.*
import kr.kua.evadecoration.placeholder.TunaLevelExpansion
import kr.kua.evadecoration.service.CosmeticHelperService
import kr.kua.evadecoration.service.NameTagService
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


lateinit var evaDecoration: EvaDecoration
lateinit var economy: Economy

class EvaDecoration : JavaPlugin() {

    companion object {
        @get:Synchronized
        val edPlayers: MutableMap<UUID, EDPlayer> = mutableMapOf()

        @get:Synchronized
        val clothData: MutableMap<String, EDCloth> = mutableMapOf()

        @get:Synchronized
        val chestData: MutableMap<String, EDChest> = mutableMapOf()
        val chestName: MutableMap<String, String> = mutableMapOf() // Indexing cache

        @get:Synchronized
        val badgeData: MutableMap<String, EDBadge> = mutableMapOf()
        val permissionBadges: MutableMap<String, String> = mutableMapOf() // Indexing cache
    }

    fun getDAO() = dao

    private lateinit var dao: EvaDecorationDAO
    lateinit var pluginConfig: EvaDecorationConfig
    lateinit var guiManager: GuiManager

    override fun onEnable() {
        evaDecoration = this

        pluginConfig = EvaDecorationConfig(config)
        saveDefaultConfig()

        /* Create DAO */
        try {
            dao = EvaDecorationDAO()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }

        guiManager = GuiManager()

        /* Register Listener */
        server.pluginManager.registerEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(PlayerQuitListener(), this)
        server.pluginManager.registerEvents(InventoryClickListener(), this)
        server.pluginManager.registerEvents(InventoryCloseListener(), this)
        server.pluginManager.registerEvents(InventoryDragListener(), this)
        server.pluginManager.registerEvents(BlockPlaceListener(this), this)

        /* Register Command */
        val commandListener = CommandListener()
        getCommand("evadecoration")!!.run {
            setExecutor(commandListener)
            tabCompleter = commandListener
        }
        getCommand("ed")!!.run {
            setExecutor(commandListener)
            tabCompleter = commandListener
        }

        /* Setting Up Third Party Plugins */
        setupEconomy()
        setupPlaceholderAPI()
        TunaLevelExpansion().register()

        /* Load Service Class */
        CosmeticHelperService.Instance = CosmeticHelperService()
        NameTagService.Instance = NameTagService()


        Bukkit.getConsoleSender().sendMessage("[EvaDecoration] EvaDecoration is enabled")
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            server.onlinePlayers.map {
                if (edPlayers[it.uniqueId] == null)
                    edPlayers[it.uniqueId] = EDPlayer(it, null, null)
                getDAO().findUser(it.uniqueId)
            }
        })
    }

    override fun onDisable() {
        getDAO().close()
        if (this::guiManager.isInitialized) guiManager.dispose()
        TunaLevelExpansion().unregister()

        Bukkit.getConsoleSender().sendMessage("[EvaDecoration] EvaDecoration is disabled")
    }

    private fun setupEconomy() {
        if (server.pluginManager.getPlugin("Vault") == null) {
            logger.severe("Could not find Vault! This plugin is required.")
            Bukkit.getPluginManager().disablePlugin(this)
        }

        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            logger.severe("The fault service could not be found.")
            Bukkit.getPluginManager().disablePlugin(this)
        } else economy = rsp.provider
    }

    private fun setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            logger.severe("Could not find PlaceholderAPI! This plugin is required.")
            Bukkit.getPluginManager().disablePlugin(this)
        }
    }
}