package kr.kua.evadecoration.gui.element

import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.CMPlayerMaterialData
import kr.kua.evadecoration.gui.GuiInterface
import kr.kua.evadecoration.gui.GuiManager
import kr.kua.evadecoration.gui.element.cosmetic.openCosmeticBalloonGui
import kr.kua.evadecoration.gui.element.cosmetic.openCosmeticHatGui
import kr.kua.evadecoration.gui.element.cosmetic.openDecorationBadgeGui
import kr.kua.evadecoration.util.times
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MainGui(private val player: Player) : GuiInterface {

    private val paneBlock = ItemStack(Material.STRUCTURE_VOID).apply {
        itemMeta = itemMeta.apply {
            setDisplayName(" ")
            setCustomModelData(1)
        }
    }

    private val renderList = mutableListOf(
        //First row
        paneBlock,
        null, // 1
        paneBlock,
        null, // 3
        paneBlock,
        null, // 5
        paneBlock,
        null, // 7
        paneBlock,
        //Second row
        paneBlock,
        paneBlock,
        paneBlock,
        paneBlock,
        paneBlock,
        paneBlock,
        paneBlock,
        paneBlock,
        paneBlock,
        //Third row
        null, // 18, infoItem
        paneBlock,
        null, // 20
        paneBlock,
        null, // 22
        paneBlock,
        null, // 24
        paneBlock,
        paneBlock
    )

    private val buttonHat: ItemStack = ItemStack(Material.STRUCTURE_VOID)
    private val buttonBackpack: ItemStack = ItemStack(Material.BARRIER)
    private val buttonBalloon: ItemStack = ItemStack(Material.STRUCTURE_VOID)
    private val buttonCanes: ItemStack = ItemStack(Material.BARRIER)
    private val buttonBadge: ItemStack = ItemStack(Material.STRUCTURE_VOID)
    private val buttonPet: ItemStack = ItemStack(Material.BARRIER)
    private val buttonGesture: ItemStack = ItemStack(Material.BARRIER)

    init {
        buttonHat.itemMeta = buttonHat.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}모자 치장")
            setCustomModelData(1)
        }
        buttonBackpack.itemMeta = buttonBackpack.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}등 치장")
            lore = listOf("${ChatColor.GRAY}준비중")
        }
        buttonBalloon.itemMeta = buttonBalloon.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}풍선 치장")
            setCustomModelData(1)
        }
        buttonCanes.itemMeta = buttonCanes.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}손 치장")
            lore = listOf("${ChatColor.GRAY}준비중")
        }
        buttonBadge.itemMeta = buttonBadge.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}뱃지")
            setCustomModelData(1)
        }
        buttonPet.itemMeta = buttonPet.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}펫")
            lore = listOf("${ChatColor.GRAY}준비중")
        }
        buttonGesture.itemMeta = buttonGesture.itemMeta.apply {
            setDisplayName("${ChatColor.RESET}제스처")
            lore = listOf("${ChatColor.GRAY}준비중")
        }

        renderList[1] = buttonHat
        renderList[3] = buttonBackpack
        renderList[5] = buttonBalloon
        renderList[7] = buttonCanes
        renderList[20] = buttonBadge
        renderList[22] = buttonPet
        renderList[24] = buttonGesture

//        val material = CMPlayerMaterialData(0, 0, 0)
//        try {
//            val playerClothMaterial = EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.clothMaterial
//            material.typeCan = playerClothMaterial.typeCan
//            material.typePaid = playerClothMaterial.typePaid
//            material.typeAchieve = playerClothMaterial.typeAchieve
//        } catch (e: Exception) {
//        }
        val infoItem = (Material.STRUCTURE_VOID * 1).apply {
            itemMeta = itemMeta.apply {
                setDisplayName(" ")
//                setDisplayName("\ue4e3")
                setCustomModelData(1)
//                lore = mutableListOf(
//                    "${ChatColor.WHITE}\uE4E1 옷감 : ${material.typeCan}개",
//                    "${ChatColor.WHITE}유료 옷감 : ${material.typePaid}개",
//                    "${ChatColor.WHITE}업적 옷감 : ${material.typeAchieve}개"
//                )
            }
        }
        renderList[18] = infoItem
    }

    override fun render(inv: Inventory) {
        for (i in renderList.indices)
            inv.setItem(i, renderList[i])
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.isCancelled) return
        event.isCancelled = true

        when (event.rawSlot) {
            1 -> {
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                player.openCosmeticHatGui()
            }

            5 -> {
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                player.openCosmeticBalloonGui()
            }

            20 -> {
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                player.openDecorationBadgeGui()
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        GuiManager.guiMap.remove(event.view)
    }
}

fun Player.openMainGui() {
    val inventory = Bukkit.createInventory(
        null,
        27,
        "${ChatColor.WHITE}\uF808\uec4b\uF81C\uF81A\uF818\uF801${ChatColor.GRAY}EVATUNA 데코레이션 통합 GUI"
    )
    val gui = MainGui(this)
    gui.render(inventory)

    GuiManager.guiMap[this.openInventory(inventory)!!] = gui
}
