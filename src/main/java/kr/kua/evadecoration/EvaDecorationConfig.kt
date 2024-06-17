package kr.kua.evadecoration

import com.francobm.magicosmetics.api.MagicAPI
import kr.kua.evadecoration.entity.BadgePosition
import kr.kua.evadecoration.entity.EDCosmeticType
import kr.kua.evadecoration.entity.global.*
import kr.kua.evadecoration.util.convertAmpersand
import kr.kua.evadecoration.util.toPlainText
import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class EvaDecorationConfig(conf: FileConfiguration) {
    // Mysql
    val mysqlHost = conf.getString("mysql.host").toString()
    val mysqlDB = conf.getString("mysql.db").toString()
    val mysqlUser = conf.getString("mysql.user").toString()
    val mysqlPassword = conf.getString("mysql.password").toString()

    init {
        val fc = YamlConfiguration()

        /*
         * cosmetics
         */
        val cosmeticConfig = File(evaDecoration.dataFolder, "cosmetics.yml")
        if (!cosmeticConfig.exists()) {
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] cosmetics.yml not found. make it...")
            evaDecoration.saveResource("cosmetics.yml", false)
        }

        fc.load(cosmeticConfig)
        for (str: String in fc.getKeys(false)) {
            val section = fc.getConfigurationSection(str)!!
            section.getKeys(false).map {
                val cosmetic = section.getConfigurationSection(it)!!
                try {
                    var cmtag: List<String>? = null
                    try {
                        cmtag = cosmetic.getStringList("cmtag")
                    } catch (_: Exception) { }

                    var disassembleItem: List<String>? = null
                    try {
                        disassembleItem = cosmetic.getStringList("disassemble_items")
                    } catch (_: Exception) { }

                    val cosmeticName = cosmetic.getString("name")!!.convertAmpersand()
                    val cosmeticId = cosmetic.getString("cosmetic_id")!!
                    val itemStack = MagicAPI.getCosmeticItem(cosmeticId)
                    val itemMeta = itemStack.itemMeta
                    itemMeta.displayName(Component.text(cosmeticName))
                    itemStack.itemMeta = itemMeta

                    EvaDecoration.clothData[it.toString()] = EDCloth(
                        it.toString(),
                        cosmeticName,
                        cosmetic.getString("description")!!.convertAmpersand(),
                        cosmeticId,
                        EDCosmeticType.valueOf(cosmetic.getString("cosmetic_type")!!),
                        cmtag,
                        itemStack,
                        cosmetic.getBoolean("possible_color"),
                        cosmetic.getBoolean("possible_disassemble"),
                        disassembleItem
                    )
                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] Load cosmetic : $it(${cosmetic.getString("name")})"
                    )
                } catch (ex: Exception) {
                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] Occur error while load cosmetic : $it(${cosmetic.getString("name")})"
                    )
                }
            }
        }

        /*
         * chest
         */
        val chestConfig = File(evaDecoration.dataFolder, "chests.yml")
        if (!chestConfig.exists()) {
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] chests.yml not found. make it...")
            evaDecoration.saveResource("chests.yml", false)
        }

        fc.load(chestConfig)
        for (str: String in fc.getKeys(false)) {
            val section = fc.getConfigurationSection(str)!!
            section.getKeys(false).map {
                val chest = section.getConfigurationSection(it)!!
                try {
                    val items = chest.getStringList("items")
                    EvaDecoration.chestData[it.toString()] = EDChest(
                        it.toString(),
                        chest.getString("name")!!.convertAmpersand(),
                        run {
                            val description = chest.getString("description")
                            if (!description.isNullOrEmpty()) // ""도 null로 넣기
                                chest.getString("description")!!.convertAmpersand()
                            else null
                        },
                        chest.getDouble("openPrice"),
                        items.map { item ->
                            val split = item.split(":")
                            ChestItem(
                                split[1],
                                split[2].toFloat()
                            )
                        }
                    )

                    EvaDecoration.chestName[chest.getString("name")!!.toPlainText()] = it.toString()

                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] Load chest : $it(${chest.getString("name")})"
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] Occur error while load chest : $it(${chest.getString("name")})"
                    )
                }
            }
        }

        /*
         * badge
         */
        val badgeConfig = File(evaDecoration.dataFolder, "badges.yml")
        if (!badgeConfig.exists()) {
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] badges.yml not found. make it...")
            evaDecoration.saveResource("badges.yml", false)
        }

        fc.load(badgeConfig)
        for (str: String in fc.getKeys(false)) {
            val section = fc.getConfigurationSection(str)!!
            section.getKeys(false).map {
                val badge = section.getConfigurationSection(it)!!
                try {
                    var disassembleItem: List<String>? = null
                    try {
                        disassembleItem = badge.getStringList("disassemble_items")
                    } catch (_: Exception) { }

                    val node = badge.getString("auto_permission")

                    EvaDecoration.badgeData[it.toString()] = EDBadge(
                        it.toString(),
                        badge.getString("name")!!.convertAmpersand(),
                        badge.getString("description")!!.convertAmpersand(),
                        badge.getString("code")!!,
                        BadgePosition.valueOf(badge.getString("position")!!.uppercase()),
                        badge.getBoolean("possible_disassemble"),
                        disassembleItem,
                        badge.getString("name_color"),
                        node
                    )

                    if (!node.isNullOrEmpty()) {
                        EvaDecoration.permissionBadges[node] = it.toString()
                    }

                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] Load badge : $it(${badge.getString("name")})"
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] Occur error while load badge : $it(${badge.getString("name")})"
                    )
                }
            }
        }
    }
}