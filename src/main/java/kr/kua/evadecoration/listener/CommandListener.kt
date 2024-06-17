package kr.kua.evadecoration.listener

import com.francobm.magicosmetics.api.MagicAPI
import com.google.gson.GsonBuilder
import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.EvaDecorationConfig
import kr.kua.evadecoration.entity.*
import kr.kua.evadecoration.entity.global.EDChest.Companion.toItemStack
import kr.kua.evadecoration.entity.global.EDPlayer
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.gui.element.openMainGui
import kr.kua.evadecoration.service.EvaDecorationDAO
import kr.kua.evadecoration.util.MagicCosmeticUtil
import kr.kua.evadecoration.util.times
import me.clip.placeholderapi.PlaceholderAPI
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class CommandListener : CommandExecutor, TabCompleter {
    private val gsonPrettier = GsonBuilder().setPrettyPrinting().create()

    override fun onCommand(
        commandSender: CommandSender, command: Command,
        label: String, args: Array<String>
    ): Boolean {
        if (label.equals("evadecoration", ignoreCase = true)
            || label.equals("ed", ignoreCase = true)
        ) {
            if (args.isEmpty()) {
                (commandSender as Player).openMainGui()
            } else {
                if (!(commandSender as Player).isOp) {
                    commandSender.sendMessage("[EvaDecoration] You don't have permission.")
                    return false
                }

                when (args[0]) {
                    "help" -> {
                        commandSender.sendMessage("/${label} list - 목록")
                        commandSender.sendMessage("/${label} force - 다른 유저에게 강제로 무언갈 하고싶나요?")
                        commandSender.sendMessage("/${label} give - 지급 관련")
                        commandSender.sendMessage("/${label} reload - cosmetics.yml 리로드")
                        commandSender.sendMessage("/${label} test, test2 - 디버깅용")
                        commandSender.sendMessage("/${label} clear - 내 데이터 초기화 [!!!주의!!!]")
                    }

                    "give" -> {
                        if (args.size < 3) {
                            commandSender.sendMessage("/${label} give <player> <cloth/chest/badge> [color]")
                            commandSender.sendMessage("color(Cloth) : hex color code(without sharp)")
                        } else {
                            when (args[0]) {
                                "give" -> {
                                    val player = Bukkit.getPlayer(args[1])
                                    if (player != null) {
                                        val cloth = EvaDecoration.clothData[args[2]]
                                        if (cloth != null) {
                                            val color = if (args.size == 4) args[3] else "FFFFFF"
                                            val cmPlayer = EvaDecoration.edPlayers[player.uniqueId]
                                            if (cmPlayer == null) {
                                                commandSender.sendMessage("[EvaDecoration] EDPlayer is not found.")
                                                return false
                                            }

                                            val userMagicCosmetic = UserMagicCosmetic(
                                                args[2],
                                                color,
                                                false,
                                                System.currentTimeMillis() / 1000,
                                            )

                                            if (cmPlayer.clothes == null) {
                                                cmPlayer.clothes = EDPlayerDataSet(
                                                    mutableListOf(userMagicCosmetic),
                                                    mutableListOf()
                                                )
                                            } else {
                                                cmPlayer.clothes!!.magicCosmeticClothes!!.add(userMagicCosmetic)
                                            }

                                            evaDecoration.getDAO().update(player.uniqueId, cmPlayer.clothes!!)
                                        } else {
                                            val chest = EvaDecoration.chestData[args[2]]
                                            if (chest != null) {
                                                val chsetIS = chest.toItemStack()
                                                player.inventory.addItem(chsetIS)
                                            } else {
                                                val cmPlayer = EvaDecoration.edPlayers[player.uniqueId]
                                                if (cmPlayer == null) {
                                                    commandSender.sendMessage("[EvaDecoration] EDPlayer is not found.")
                                                    return false
                                                }

                                                val badge = EvaDecoration.badgeData[args[2]]
                                                if (badge != null) {
                                                    val userBadge = UserBadge(
                                                        args[2],
                                                        true,
                                                        System.currentTimeMillis() / 1000,
                                                    )
                                                    if (cmPlayer.clothes == null) {
                                                        cmPlayer.clothes = EDPlayerDataSet(
                                                            mutableListOf(),
                                                            mutableListOf(userBadge)
                                                        )
                                                    } else {
                                                        cmPlayer.clothes!!.badges!!.add(userBadge)
                                                    }
                                                } else {
                                                    commandSender.sendMessage("Critical not found")
                                                }
                                            }
                                        }
                                    } else {
                                        commandSender.sendMessage("Player not found")
                                    }
                                }

                                else -> {
                                    commandSender.sendMessage("/${label} admin give <player> <cloth> [color]")
                                    commandSender.sendMessage("color : hex color code(without sharp)")
                                }
                            }
                        }
                    }

                    "force" -> {
                        if (args.size < 4) {
                            commandSender.sendMessage("/${label} force equip <player> <cloth> [color]")
                            commandSender.sendMessage("/${label} force unequip <player> <type>")
                            commandSender.sendMessage("type : Hat, Walking-stick, Bag, Balloon")
                            commandSender.sendMessage("color : hex color code(without sharp)")
                        } else {
                            when (args[1]) {
                                "equip" -> {
                                    val player = Bukkit.getPlayer(args[2])
                                    if (player != null) {
                                        val cloth = EvaDecoration.clothData[args[3]]
                                        if (cloth != null) {
                                            val color = if (args.size == 5) args[4] else "FFFFFF"
                                            if (!MagicAPI.hasCosmetic(player, cloth.cosmeticId))
                                                Bukkit.dispatchCommand(
                                                    Bukkit.getConsoleSender(),
                                                    "cosmetics add ${player.name} ${cloth.cosmeticId}"
                                                )

                                            try {
                                                MagicAPI.EquipCosmetic(player, cloth.cosmeticId, "#$color", true)
                                            } catch (ex: Exception) {
                                                commandSender.sendMessage("[EvaDecoration] An Error Occurred")
                                                evaDecoration.server.consoleSender.sendMessage("[evaDecoration] An Error Occurred : ${ex.printStackTrace()}")
                                            }
                                        } else {
                                            commandSender.sendMessage("Cloth not found")
                                        }
                                    } else {
                                        commandSender.sendMessage("Player not found")
                                    }
                                }

                                "unequip" -> {
                                    val player = Bukkit.getPlayer(args[2])
                                    if (player != null) {
                                        val type = EDCosmeticType.valueOf(args[3].uppercase())
                                        MagicAPI.UnEquipCosmetic(player, MagicCosmeticUtil.clothTypeToMagicType(type))
                                    } else {
                                        commandSender.sendMessage("Player not found")
                                    }
                                }

                                else -> {
                                    commandSender.sendMessage("/${label} force equip <player> <cloth> [color]")
                                    commandSender.sendMessage("/${label} force unequip <player> <type>")
                                    commandSender.sendMessage("type : Hat, Walking-stick, Bag, Balloon")
                                    commandSender.sendMessage("color : hex color code(without sharp)")
                                }
                            }
                        }
                    }

                    "test" -> {
                        val player = commandSender
                        if (EvaDecoration.edPlayers[player.uniqueId] == null)
                            EvaDecoration.edPlayers[player.uniqueId] = EDPlayer(player, null, null)
                        evaDecoration.getDAO().findUser(player.uniqueId)

                        EvaDecoration.edPlayers.map {
                            commandSender.sendMessage("[EvaDecoration] (${it.key}) - ${it.value}")
                        }
                    }

                    "test2" -> {
                        val player = evaDecoration.server.getPlayer(commandSender.name)
                        val item = (Material.CHEST * 1).apply {
                            itemMeta = itemMeta.apply {
                                addEnchant(Enchantment.DURABILITY, 0, true)
                                addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS)
                                addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            }
                        }
                        if (player != null) {
                            player.inventory.addItem(item)
                        }
                    }

                    "test3" -> {
                        val tunaLevel = PlaceholderAPI.containsPlaceholders("%player_tunalevel%")
                        commandSender.sendMessage("%player_tunalevel% is $tunaLevel")
                        PlaceholderAPI.setPlaceholders(commandSender, "%player_badge%")
                    }

                    "list" -> {
                        if (args.size < 2) {
                            commandSender.sendMessage("/${label} list <type>")
                            commandSender.sendMessage("type : Hat, Walking-stick, Bag, Balloon, Chest")
                        } else {
                            when (args[1]) {
                                "Hat" -> {
                                    EvaDecoration.clothData.filter { it.value.edCosmeticType == EDCosmeticType.HAT }.map {
                                        commandSender.sendMessage("[EvaDecoration] ${it.key} - ${it.value.name}")
                                    }
                                }

                                "Walking-stick" -> {
                                    EvaDecoration.clothData.filter { it.value.edCosmeticType == EDCosmeticType.WALKING_STICK }
                                        .map {
                                            commandSender.sendMessage("[EvaDecoration] ${it.key} - ${it.value.name}")
                                        }
                                }

                                "Bag" -> {
                                    EvaDecoration.clothData.filter { it.value.edCosmeticType == EDCosmeticType.BAG }.map {
                                        commandSender.sendMessage("[EvaDecoration] ${it.key} - ${it.value.name}")
                                    }
                                }

                                "Balloon" -> {
                                    EvaDecoration.clothData.filter { it.value.edCosmeticType == EDCosmeticType.BALLOON }.map {
                                        commandSender.sendMessage("[EvaDecoration] ${it.key} - ${it.value.name}")
                                    }
                                }

                                "Chest" -> {
                                    EvaDecoration.chestData.map {
                                        commandSender.sendMessage("[EvaDecoration] ${it.key} - ${it.value.name}")
                                        it.value.clothIndex.map { item ->
                                            commandSender.sendMessage("> ${item.cmCosmeticId}:${item.chance}")
                                        }
                                    }
                                }

                                "Badge" -> {
                                    EvaDecoration.badgeData.map {
                                        commandSender.sendMessage("[EvaDecoration] ${it.value.code} ${it.key}: ${it.value.name}")
                                    }
                                }

                                else -> {
                                    commandSender.sendMessage("${ChatColor.RED}${args[1]} is Unknown type!")
                                }
                            }
                        }
                    }

                    "reload" -> {
                        evaDecoration.pluginConfig = EvaDecorationConfig(evaDecoration.config)
                        commandSender.sendMessage("[EvaDecoration] Config reloaded...")
                        commandSender.sendMessage("[EvaDecoration] ${EvaDecoration.clothData.size} cosmetics have been loaded")
                        commandSender.sendMessage("[EvaDecoration] Reload completed successfully...")
                    }

                    "clear" -> {
                        evaDecoration.getDAO().update(
                            commandSender.uniqueId,
                            EDPlayerDataSet(mutableListOf(), mutableListOf())
                        )
                        EvaDecoration.edPlayers[commandSender.uniqueId]?.clothes = EDPlayerDataSet(
                            mutableListOf(),
                            mutableListOf()
                        )
                        commandSender.sendMessage("[EvaDecoration] ${commandSender}'s data has been cleared.")
                    }
                }
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (!(sender as Player).isOp) {
            return null
        }

        when (args.size) {
            1 -> {
                return mutableListOf("help", "force", "give", "list", "reload", "test", "test2", "clear")
            }

            2 -> {
                when (args[0]) {
                    "force" -> {
                        return mutableListOf("equip", "unequip")
                    }

                    "give" -> {
                        return evaDecoration.server.onlinePlayers.map { it.name }.toMutableList()
                    }

                    "list" -> {
                        return mutableListOf("Hat", "Walking-stick", "Bag", "Balloon", "Chest", "Badge")
                    }
                }
            }

            3 -> {
                when (args[0]) {
                    "force" -> {
                        return evaDecoration.server.onlinePlayers.map { it.name }.toMutableList()
                    }

                    "give" -> {
                        return (EvaDecoration.chestData.map { it.key } + EvaDecoration.clothData.map { it.key }).toMutableList()
                    }
                }
            }

            4 -> {
                when (args[0]) {
                    "force" -> {
                        when (args[1]) {
                            "equip" -> {
                                return EvaDecoration.clothData.map { it.key }.toMutableList()
                            }

                            "unequip" -> {
                                return mutableListOf("Hat", "Walking-stick", "Bag", "Balloon", "Badge")
                            }
                        }
                    }
                }
            }
        }

        return null
    }
}