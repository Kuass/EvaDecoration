package kr.kua.evadecoration.gui.element.cosmetic.deep

import kr.kua.evadecoration.entity.global.EDCloth
import kr.kua.evadecoration.entity.UserMagicCosmetic
import kr.kua.evadecoration.entity.EDCosmeticType
import kr.kua.evadecoration.gui.GuiInterface
import kr.kua.evadecoration.gui.GuiManager
import kr.kua.evadecoration.gui.element.cosmetic.openCosmeticBalloonGui
import kr.kua.evadecoration.gui.element.cosmetic.openCosmeticHatGui
import kr.kua.evadecoration.util.decoMessage
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

class EditorGui(
    private val player: Player,
    private val edCloth: EDCloth,
    private val userMagicCosmetic: UserMagicCosmetic
) : GuiInterface {

    private val paneBlock = ItemStack(Material.STRUCTURE_VOID).apply {
        itemMeta = itemMeta.apply {
            setDisplayName(" ")
            setCustomModelData(1)
        }
    }
    private val previousPageButton = ItemStack(Material.FEATHER).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}이전 페이지")
            setCustomModelData(10037)
        }
    }

    private val renderList = mutableListOf(
        //First row
        paneBlock,
        paneBlock, // 1
        (Material.OAK_SIGN * 1).apply { itemMeta = itemMeta.apply { setDisplayName("대상 치장") } },
        paneBlock, // 3
        paneBlock,
        paneBlock, // 5
        paneBlock,
        paneBlock, // 7
        paneBlock,
        //Second row
        previousPageButton,
        paneBlock,
        edCloth.magicCosmeticComponent,
        paneBlock,
        paneBlock, // 13
        (Material.GLASS_PANE * 1).apply {
            itemMeta = itemMeta.apply {
                setDisplayName("${ChatColor.RESET}염색")
                lore = mutableListOf("${ChatColor.RED}염색 불가 치장", "${ChatColor.GRAY}개발 중입니다!")
            }
        },
        (Material.GLASS_PANE * 1).apply {
            itemMeta = itemMeta.apply {
                setDisplayName("${ChatColor.RESET}분해")
            }
        },
        paneBlock, // 16
        paneBlock,
        //Third row
        paneBlock,
        paneBlock,
        paneBlock, // 20
        paneBlock,
        paneBlock, // 22
        paneBlock,
        paneBlock, // 24
        paneBlock,
        paneBlock
    )

    override fun render(inv: Inventory) {
        if (!edCloth.possibleDisassemble) {
            renderList[15] = (Material.GLASS_PANE * 1).apply {
                itemMeta = itemMeta.apply {
                    lore = mutableListOf("${ChatColor.RED}분해 불가 치장")
                }
            }
        }

        for (i in renderList.indices)
            inv.setItem(i, renderList[i])

    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.isCancelled) return
        event.isCancelled = true

        when (event.rawSlot) {
            9 -> {
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                when (edCloth.edCosmeticType) {
                    EDCosmeticType.HAT -> player.openCosmeticHatGui()
                    EDCosmeticType.BALLOON -> player.openCosmeticBalloonGui()
                    else -> {
                        player.decoMessage("${ChatColor.RED}아직 지원하지 않는 치장입니다.")
                        player.closeInventory()
                    }
                }
            }

            15 -> {
                if (!edCloth.possibleDisassemble) return
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                player.openCosmeticDisassembleGui(edCloth, userMagicCosmetic)
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        GuiManager.guiMap.remove(event.view)
    }
}

fun Player.openCosmeticEditorGui(edCloth: EDCloth, userMagicCosmetic: UserMagicCosmetic) {
    val inventory = Bukkit.createInventory(null, 27, "치장 수정")
    val gui = EditorGui(this, edCloth, userMagicCosmetic)
    gui.render(inventory)

    GuiManager.guiMap[this.openInventory(inventory)!!] = gui
}