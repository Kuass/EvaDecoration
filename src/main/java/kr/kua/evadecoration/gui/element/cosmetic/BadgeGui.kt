package kr.kua.evadecoration.gui.element.cosmetic

import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.UserBadge
import kr.kua.evadecoration.entity.global.EDBadge.Companion.toItemStack
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.gui.GuiInterface
import kr.kua.evadecoration.gui.GuiManager
import kr.kua.evadecoration.gui.element.cosmetic.deep.openCosmeticEditorGui
import kr.kua.evadecoration.gui.element.openMainGui
import kr.kua.evadecoration.service.CosmeticHelperService
import kr.kua.evadecoration.service.NameTagService
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

class BadgeGui(private val player: Player) : GuiInterface {

    private var currentPage = 0
    private val badgeIndex: MutableList<Pair<String, UserBadge>> = mutableListOf()
    private var badgeList = mutableListOf<UserBadge>()

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
            lore = mutableListOf("${ChatColor.YELLOW}Shift + Left Click${ChatColor.WHITE}으로 치장 수정창 열기")
        }
    }

    override fun render(inv: Inventory) {
//            if (player.isOp) NametagEdit.getApi().setPrefix(player, "§f\uf027 §c")
//            else if (player.hasPermission("group.partner")) NametagEdit.getApi().setPrefix(player, "§f\ue4e0 §9")
        badgeList = EvaDecoration.edPlayers[player.uniqueId]?.clothes?.badges ?: mutableListOf()
        EvaDecoration.permissionBadges.map { a ->
            if (player.hasPermission(a.key)) {
                val has = badgeList.filter {
                    it.id == a.value // 갖고있지 않은 뱃지만
                }
                if (has.isEmpty()) {
                    EvaDecoration.edPlayers[player.uniqueId]?.clothes?.badges!!.add(UserBadge(a.value, false, System.currentTimeMillis() / 1000))
                    evaDecoration.getDAO().update(player.uniqueId, EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!)
                }
            }
        }

        inv.setItem(47, infoSign)
        inv.setItem(49, homeButton)

        pageRender(inv)
    }

    private fun pageRender(inv: Inventory) {
        val start = currentPage * 45
        badgeIndex.clear()
        if (badgeList.isNotEmpty()) {
            val length = min(badgeList.size - start, 45)
            var lastI = 0
            for (i in 0 until length) {
                val userBadge = badgeList[start + i]
                val badge = EvaDecoration.badgeData[userBadge.id]
                if (badge == null) {
                    evaDecoration.server.consoleSender.sendMessage(
                        "[EvaDecoration] badgeData ${userBadge.id} is not exist!"
                    )
                    player.decoMessage("문제 발생. 제보 바랍니다.")
                    player.inventory.close()
                    return
                }

                val itemStack = badge.toItemStack().apply {
                    itemMeta = itemMeta.apply {
                        val lores = mutableListOf<String>()
                        lore!!.map { lores.add(it) }
                        if (userBadge.isDress) lores.add("${ChatColor.GOLD}>> 착용 중")
                        lore = lores
                    }
                }
                badgeIndex.add(badge.id to userBadge)
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

        if ((badgeList.size - start) > 45)
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
                if (badgeIndex.size <= clicked) return

                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
                val badgeIndex = badgeIndex[clicked]
                val badge = EvaDecoration.badgeData[badgeIndex.first]!!
                if (event.isShiftClick) {
//                    player.openCosmeticEditorGui(clothData, cloth.second)
                } else {
                    event.view.close()
                    if (badgeIndex.second.isDress) {
                        NameTagService.Instance.unequip(player, badge, badgeIndex.second, true)
                    } else {
                        NameTagService.Instance.equip(player, badge, badgeIndex.second)
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
                if ((badgeList.size - start) > 45) {
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

fun Player.openDecorationBadgeGui() {
    val inventory = Bukkit.createInventory(
        null,
        54,
        "${ChatColor.WHITE}\uF808\uec4c\uF81C\uF81A\uF818\uF801${ChatColor.GRAY}보유한 뱃지 치장"
    )
    val gui = BadgeGui(this)
    gui.render(inventory)

    GuiManager.guiMap[this.openInventory(inventory)!!] = gui
}