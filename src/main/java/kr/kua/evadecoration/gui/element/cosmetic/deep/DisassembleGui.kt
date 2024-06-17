package kr.kua.evadecoration.gui.element.cosmetic.deep

import io.lumine.mythic.bukkit.MythicBukkit
import kr.kua.evadecoration.entity.global.EDCloth
import kr.kua.evadecoration.entity.UserMagicCosmetic
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.gui.GuiInterface
import kr.kua.evadecoration.gui.GuiManager
import kr.kua.evadecoration.service.CosmeticHelperService
import kr.kua.evadecoration.util.decoMessage
import kr.kua.evadecoration.util.isInventoryFull
import kr.kua.evadecoration.util.makeDisassembleMessage
import kr.kua.evadecoration.util.times
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class DisassembleGui(
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

    private val noPane =
        (Material.GREEN_STAINED_GLASS_PANE * 1).apply { itemMeta = itemMeta.apply { setDisplayName("${ChatColor.WHITE}취소") } }
    private val okPane =
        (Material.RED_STAINED_GLASS_PANE * 1).apply { itemMeta = itemMeta.apply { setDisplayName("${ChatColor.WHITE}분해") } }
    private val renderList = mutableListOf(
        //First row
        noPane,
        noPane, // 1
        noPane,
        paneBlock, // 3
        paneBlock,
        paneBlock, // 5
        okPane,
        okPane, // 7
        okPane,
        //Second row
        noPane,
        noPane,
        noPane,
        paneBlock,
        edCloth.magicCosmeticComponent, // 13
        paneBlock,
        okPane,
        okPane, // 16
        okPane,
        //Third row
        noPane,
        noPane,
        noPane, // 20
        paneBlock,
        paneBlock, // 22
        paneBlock,
        okPane, // 24
        okPane,
        okPane
    )

    override fun render(inv: Inventory) {
        for (i in renderList.indices)
            inv.setItem(i, renderList[i])
    }

    override fun onClick(event: InventoryClickEvent) {
        if (event.isCancelled) return
        event.isCancelled = true

        when (event.rawSlot) {
            in 0..2 -> clickNo()
            in 9..11 -> clickNo()
            in 18..20 -> clickNo()
            in 6..8 -> clickYes()
            in 15..17 -> clickYes()
            in 24..26 -> clickYes()
        }
    }

    private fun clickNo() {
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
        player.openCosmeticEditorGui(edCloth, userMagicCosmetic)
    }

    private fun clickYes() {
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
        player.inventory.close()

        if (!edCloth.possibleDisassemble) return

        player.decoMessage("${edCloth.name}${ChatColor.RESET}을(를) 분해합니다..")
        edCloth.disassembleItems!!.map {
            val mainSplit = it.split(":")
            val nameSplit = mainSplit[0].split("-")

            val chance = mainSplit[2].toInt()
            val rand = Random().nextInt(100)
            if (rand <= chance) {
                val amount = mainSplit[1].toInt()
                if (nameSplit[0] == "fabric") {
                    when (nameSplit[1]) {
                        "can" -> {
                            player.decoMessage(makeDisassembleMessage("캔 옷감", amount))
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveitemrand ${player.name} cosmetic can_fabric $amount 0")
                            evaDecoration.logger.info("[EvaDecoration] [Disassemble] ${player.name} dissected ${edCloth.id} and got $amount ${nameSplit[0]}.")
                        }

                        "paid" -> {
//                            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.clothMaterial.typePaid += amount
                            player.decoMessage(makeDisassembleMessage("유료 옷감", amount))
                            evaDecoration.logger.info("[EvaDecoration] [Disassemble] ${player.name} dissected ${edCloth.id} and got $amount ${nameSplit[0]}.")
                        }

                        else -> throw IllegalArgumentException("존재하지 않는 옷감")
                    }
                } else {
                    val itemStack = MythicBukkit.inst().itemManager.getItemStack(nameSplit[0])
                    player.giveItem(itemStack, amount)
                    evaDecoration.logger.info("[EvaDecoration] [Disassemble] ${player.name} dissected ${edCloth.id} and got $amount ${nameSplit[0]}.")
                }
            }
        }

        CosmeticHelperService.Instance.takeCosmetic(player, edCloth, userMagicCosmetic)
    }

    override fun onClose(event: InventoryCloseEvent) {
        GuiManager.guiMap.remove(event.view)
    }

    private fun Player.giveItem(itemStack: ItemStack, amount: Int) {
        itemStack.amount = amount

        if (!this.isInventoryFull()) {
            this.inventory.addItem(itemStack)
        } else {
            this.world.dropItem(this.location.add(0.0, 1.0, 0.0), itemStack)
            this.decoMessage("인벤토리가 가득차 플레이어님의 위치에 아이템이 떨어집니다.")
        }
        this.decoMessage(
            makeDisassembleMessage(
                PlainTextComponentSerializer.plainText().serialize(
                    itemStack.displayName()
                ), amount
            )
        )
    }
}

fun Player.openCosmeticDisassembleGui(edCloth: EDCloth, userMagicCosmetic: UserMagicCosmetic) {
    val inventory = Bukkit.createInventory(null, 27, "치장 분해")
    val gui = DisassembleGui(this, edCloth, userMagicCosmetic)
    gui.render(inventory)

    GuiManager.guiMap[this.openInventory(inventory)!!] = gui
}