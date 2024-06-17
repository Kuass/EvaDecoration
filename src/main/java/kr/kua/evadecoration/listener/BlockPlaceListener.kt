package kr.kua.evadecoration.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.BlockPosition
import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.economy
import kr.kua.evadecoration.entity.global.EDPlayer
import kr.kua.evadecoration.entity.UserMagicCosmetic
import kr.kua.evadecoration.entity.EDPlayerDataSet
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.util.decoMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.lang.reflect.InvocationTargetException


class BlockPlaceListener(private val instance: EvaDecoration) : Listener {

    private val chestLocations: MutableMap<String, ChestLocation> = mutableMapOf()
    private fun Location.toXYZ(): String {
        return "${this.blockX},${this.blockY},${this.blockZ}"
    }

    data class ChestLocation(val xyz: String, val needRecover: Boolean, val recentBlock: Material?)
    // chestLocations key = xyz: String  ===  ChsetLocation.xyz   SAME!!!

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.type == Material.CHEST) {
            val chestNaturalLocation = event.block.location.toCenterLocation()
            if (chestLocations.contains(chestNaturalLocation.toXYZ())) {
                event.isCancelled = true
                // 상자를 부술 수 없음
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInteractEvent(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        if (event.clickedBlock == null) return

        if (event.clickedBlock!!.type == Material.CHEST && chestLocations.contains(event.clickedBlock!!.location.toXYZ())) {
            // 상자를 열 수 없음
            event.isCancelled = true
            return
        }

        if (event.item == null || event.item!!.type != Material.CHEST) return

        val meta = event.item!!.itemMeta!!
        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)
            && meta.hasItemFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
            && meta.hasEnchant(Enchantment.DURABILITY)
            && meta.hasDisplayName()
        ) {
            val block = event.clickedBlock!!.location.add(0.0, 1.0, 0.0).block
            val player = event.player
            if (block.type != Material.AIR
                || event.clickedBlock!!.location.add(0.0, 2.0, 0.0).block.type != Material.AIR
                || event.clickedBlock!!.location.add(0.0, 3.0, 0.0).block.type != Material.AIR
            ) {
                player.decoMessage("상자를 설치할 수 없는 장소입니다.")
                event.isCancelled = true
                return
            }

            val chestData = EvaDecoration.chestData[EvaDecoration.chestName[
                PlainTextComponentSerializer.plainText().serialize(meta.displayName()!!)
            ]]
            if (chestData == null) {
                player.decoMessage("데이터에 존재하지 않는 상자입니다. 관리자에게 문의하세요.")
                event.isCancelled = true
                return
            }

            val openedCosmeticDataName = chestData.openChest().cmCosmeticId
            val resultCloth = EvaDecoration.clothData[openedCosmeticDataName]
            if (resultCloth == null) {
                player.decoMessage("상자 내부 설정 데이터가 잘못되었습니다. 관리자에게 문의하세요.")
                event.isCancelled = true
                return
            }

            event.isCancelled = true
            if (economy.getBalance(player) < chestData.openPrice) {
                player.decoMessage("상자를 열기 위한 캔이 부족합니다.")
                return
            }
            economy.withdrawPlayer(player, chestData.openPrice)

            evaDecoration.logger.info("[EvaDecoration] [ChestOpen] ${player.name} opened the ${chestData.id} chest and got a $openedCosmeticDataName cosmetic and paid ${chestData.openPrice} can.")

            val chestNaturalLocation = block.location.toCenterLocation()
            val loc = block.location
            player.world.playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1f, 1f)
            player.world.spawnParticle(Particle.SPELL_INSTANT, loc.add(0.5, 0.5, 0.5), 80)

            val downLoc = loc.clone().add(0.0, -1.0, 0.0)
            if (downLoc.isBlock && downLoc.block.type == Material.DIRT_PATH)
                chestLocations[chestNaturalLocation.toXYZ()] = ChestLocation(chestNaturalLocation.toXYZ(), true, downLoc.block.type)
            else
                chestLocations[chestNaturalLocation.toXYZ()] = ChestLocation(chestNaturalLocation.toXYZ(), false, null)

            event.item!!.amount -= 1
            event.blockFace.let {
                block.type = Material.CHEST
                block.blockData = Bukkit.createBlockData(Material.CHEST)
            }
//            block.state.blockData.rotate(StructureRotation.CLOCKWISE_90)
            block.state.update(true)
            // TODO: 상자를 돌리는, 방향 코드 짜기...

            val cmPlayer = EvaDecoration.edPlayers[event.player.uniqueId] ?: run {
                val cmPlayer = EDPlayer(event.player, null, null)
                EvaDecoration.edPlayers[event.player.uniqueId] = cmPlayer
                evaDecoration.getDAO().findUser(event.player.uniqueId)
                cmPlayer
            }

            Bukkit.getScheduler().runTaskLaterAsynchronously(
                instance, Runnable {
                    player.world.playSound(loc, Sound.BLOCK_CHEST_OPEN, 1f, 1f)
                    var libPacket = PacketContainer(PacketType.Play.Server.BLOCK_ACTION).apply {
                        blockPositionModifier.write(0, BlockPosition(loc.blockX, loc.blockY, loc.blockZ))
                        integers.write(0, 1)
                        integers.write(1, 1) // 1 = opened
                        blocks.write(0, block.type)
                    }

                    val distanceSquared = 32 * 32
                    val manager = ProtocolLibrary.getProtocolManager()
                    val item = resultCloth.magicCosmeticComponent
                    try {
                        for (player in block.world.players) {
                            if (player.location.distanceSquared(loc) < distanceSquared) {
                                manager.sendServerPacket(player, libPacket)
                            }
                        }

                        spawnFloatingItem(loc, item, loc.yaw, loc.pitch)

                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }

                    player.decoMessage(
                        "축하드립니다! ${
                            PlainTextComponentSerializer.plainText().serialize(item.itemMeta!!.displayName()!!)
                        }${ChatColor.WHITE}을(를) 얻었습니다."
                    )

                    try {
                        if (cmPlayer.clothes == null
                            || cmPlayer.clothes!!.magicCosmeticClothes!!!!.size == 0)
                            player.decoMessage("${ChatColor.YELLOW}와우! ${ChatColor.WHITE}처음 치장을 얻으셨군요? /ed 명령어를 통해 치장을 착용해보세요!")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    player.world.spawnParticle(Particle.WAX_OFF, loc, 200)
                    Thread.sleep(5000)
                    player.world.playSound(loc, Sound.BLOCK_CHEST_CLOSE, 1f, 1f)

                    loc.add(0.0, -1.0, 0.0)
                    libPacket = PacketContainer(PacketType.Play.Server.BLOCK_ACTION).apply {
                        blockPositionModifier.write(0, BlockPosition(loc.blockX, loc.blockY, loc.blockZ))
                        integers.write(0, 1)
                        integers.write(1, 0)
                        blocks.write(0, block.type)
                    }

                    try {
                        for (p in block.world.players) {
                            if (p.location.distanceSquared(loc) < distanceSquared) {
                                manager.sendServerPacket(p, libPacket)
                            }
                        }
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }

                    Bukkit.getScheduler().runTaskLater(
                        instance, Runnable {
                            loc.block.type = Material.AIR
                            if (chestLocations[chestNaturalLocation.toXYZ()]!!.needRecover) {
                                loc.block.type = chestLocations[chestNaturalLocation.toXYZ()]!!.recentBlock!!
                            }
                            chestLocations.remove(chestNaturalLocation.toXYZ())

                            if (cmPlayer.clothes == null) {
                                cmPlayer.clothes = EDPlayerDataSet(
                                    mutableListOf(
                                        UserMagicCosmetic(
                                            openedCosmeticDataName,
                                            "FFFFFF",
                                            false,
                                            System.currentTimeMillis() / 1000,
                                        )
                                    ),
                                    mutableListOf()
                                )
                            } else {
                                cmPlayer.clothes!!.magicCosmeticClothes!!.add(
                                    UserMagicCosmetic(
                                        openedCosmeticDataName,
                                        "FFFFFF",
                                        false,
                                        System.currentTimeMillis() / 1000,
                                    )
                                )
                            }

                            evaDecoration.getDAO().update(player.uniqueId, cmPlayer.clothes!!)
                        }, 20L
                    )

                },
                40L
            )

            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, Runnable {
                repeat(3) {
                    player.world.spawnParticle(Particle.SPELL_WITCH, loc, 30)
                    player.world.playSound(
                        loc,
                        "littleroom_wolfebersahd:littleroom.wolfebersahd.hammerhit",
                        0.2f,
                        1f
                    )
                    Thread.sleep(500)
                }
            }, 10L)
        }
    }

    private fun spawnFloatingItem(loc: Location, itemStack: ItemStack, yaw: Float = 0f, pitch: Float = 0f) {
        Bukkit.getScheduler().runTask(instance, Runnable {
            loc.add(0.0, 1.0, 0.0)
            val itemFrame = loc.world.spawnEntity(loc, EntityType.ITEM_FRAME) as ItemFrame
            itemFrame.teleport(loc)
            itemFrame.setFacingDirection(BlockFace.UP, true)
            itemFrame.setGravity(false)
            itemFrame.isVisible = false
            itemFrame.isFixed = true
            itemFrame.setItem(itemStack)

            Bukkit.getScheduler().runTaskLater(instance, Runnable {
                itemFrame.remove()
            }, 110L)
        })
    }

    fun getDirection(yaw: Float): String? {
        var rotation = ((yaw - 90) % 360).toDouble()
        if (rotation < 0) rotation += 360.0

        return if (-60 <= rotation && rotation < 60) {
            "S"
        } else if (60 <= rotation && rotation < 140) {
            "W"
        } else if (140 <= rotation && rotation < -120) {
            "N"
        } else if (112.5 <= rotation && rotation < 157.5) {
            "SE"
        } else if (157.5 <= rotation && rotation < 202.5) {
            "S"
        } else if (202.5 <= rotation && rotation < 247.5) {
            "SW"
        } else if (247.5 <= rotation && rotation < 292.5) {
            "W"
        } else if (292.5 <= rotation && rotation < 337.5) {
            "NW"
        } else if (337.5 <= rotation && rotation < 360.0) {
            "N"
        } else {
            null
        }
    }
}