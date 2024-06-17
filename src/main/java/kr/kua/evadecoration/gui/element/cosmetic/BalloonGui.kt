package kr.kua.evadecoration.gui.element.cosmetic

import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.UserMagicCosmetic
import kr.kua.evadecoration.entity.EDCosmeticType
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.gui.GuiInterface
import kr.kua.evadecoration.gui.GuiManager
import kr.kua.evadecoration.gui.element.cosmetic.deep.openCosmeticEditorGui
import kr.kua.evadecoration.gui.element.openMainGui
import kr.kua.evadecoration.service.CosmeticHelperService
import kr.kua.evadecoration.util.ColorUtil
import kr.kua.evadecoration.util.MagicCosmeticUtil.Companion.cmTagToString
import kr.kua.evadecoration.util.decoMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class BalloonGui(private val player: Player) : GuiInterface {

    private var currentPage = 0
    private val clothIndex: MutableList<Pair<String, UserMagicCosmetic>> = mutableListOf()
    private var balloonList = listOf<UserMagicCosmetic>()

    private val previousPageButton = ItemStack(Material.FEATHER).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}이전 페이지")
            setCustomModelData(10037)
        }
    }
    private val blockedPreviousPageButton = ItemStack(Material.FEATHER).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}이전 페이지")
            setCustomModelData(10038)
        }
    }
    private val nextPageButton = ItemStack(Material.FEATHER).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}다음 페이지")
            setCustomModelData(10039)
        }
    }
    private val blockedNextPageButton = ItemStack(Material.FEATHER).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}다음 페이지")
            setCustomModelData(10040)
        }
    }
    private val homeButton = ItemStack(Material.FEATHER).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}홈")
            setCustomModelData(10036)
        }
    }
    private val infoSign = ItemStack(Material.OAK_HANGING_SIGN).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.WHITE}\ue4e3")
            lore = mutableListOf("${ChatColor.WHITE}> Shift + 클릭으로 치장 수정창 열기")
        }
    }

    override fun render(inv: Inventory) {
        balloonList = EvaDecoration.edPlayers[player.uniqueId]?.clothes?.magicCosmeticClothes?.filter {
            EvaDecoration.clothData[it.id] != null &&
                    EvaDecoration.clothData[it.id]!!.edCosmeticType == EDCosmeticType.BALLOON
        } ?: listOf()

        inv.setItem(47, infoSign)
        inv.setItem(49, homeButton)

        pageRender(inv)
    }

    private fun pageRender(inv: Inventory) {
        val start = currentPage * 45
        clothIndex.clear()
        if (balloonList.isNotEmpty()) {
            val length = min(balloonList.size - start, 45)
            var lastI = 0
            for (i in 0 until length) {
                val item = balloonList[start + i]
                val cmClothData = EvaDecoration.clothData[item.id]
                if (cmClothData == null) {
                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] CMClothData ${item.id} is not exist!"
                    )
                    player.decoMessage("문제 발생. 제보 바랍니다.")
                    player.inventory.close()
                    return
                }

                val itemStack = cmClothData.magicCosmeticComponent
                itemStack.itemMeta = itemStack.itemMeta.apply {
                    val loreList = mutableListOf<String>()
                    cmClothData.description.split("\n").forEach { loreList.add(it) }
                    loreList.add("")
                    if (cmClothData.cmTag != null) cmTagToString(cmClothData.cmTag).forEach { loreList.add(it) }
                    loreList.add("${ChatColor.GRAY}분해 : ${if (cmClothData.possibleDisassemble) "가능" else "불가"}")
                    loreList.add("${ChatColor.GRAY}염색 : ${if (cmClothData.possibleColor) "가능" else "불가"}")
                    if (cmClothData.possibleColor) loreList.add(
                        "${ChatColor.WHITE}현재 색깔 : ${ColorUtil.translateHexColorCodes("#${item.magicCosmeticColor}")}■■■ ${ChatColor.WHITE}(#${item.magicCosmeticColor})"
                    )
                    if (item.isDress) loreList.add("${ChatColor.GOLD}>> 착용 중")

                    this.lore = loreList
                }
                clothIndex.add(item.id to item)
                inv.setItem(i, itemStack)
                lastI = i
            }

            if (lastI != 45) { // 다시 비우기
                for (i in lastI + 1..45)
                    inv.setItem(i, null)
            }
        }
        if (currentPage < 1)
            inv.setItem(45, blockedPreviousPageButton)
        else
            inv.setItem(45, previousPageButton)

        if ((balloonList.size - start) > 45)
            inv.setItem(53, nextPageButton)
        else
            inv.setItem(53, blockedNextPageButton)
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.isCancelled) return
        event.isCancelled = true

        when (val clicked = event.rawSlot) {
            49 -> {
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                player.openMainGui()
            }

            in 0..35 -> {
                if (clothIndex.size <= clicked) return

                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                val cloth = clothIndex[clicked]
                val clothData = EvaDecoration.clothData[cloth.first]!!
                if (event.isShiftClick) {
                    player.openCosmeticEditorGui(clothData, cloth.second)
                } else {
                    event.view.close()
                    if (cloth.second.isDress) {
                        CosmeticHelperService.Instance.unEquipCosmetic(player, clothData, cloth.second, true)
                    } else {
                        CosmeticHelperService.Instance.equipCosmetic(player, clothData, cloth.second)
                    }
                }
            }

            45 -> {
                if (currentPage == 0) return
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                currentPage--
                pageRender(event.inventory)
            }

            53 -> {
                val start = currentPage * 45
                if ((balloonList.size - start) > 45) {
                    currentPage++
                    player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                    pageRender(event.inventory)
                }
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        GuiManager.guiMap.remove(event.view)
    }
}

fun Player.openCosmeticBalloonGui() {
    val inventory = Bukkit.createInventory(
        null,
        54,
        "${ChatColor.WHITE}\uF808\uec4c\uF81C\uF81A\uF818\uF801${ChatColor.GRAY}보유한 풍선 치장"
    )
    val gui = BalloonGui(this)
    gui.render(inventory)

    GuiManager.guiMap[this.openInventory(inventory)!!] = gui
}