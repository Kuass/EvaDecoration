package kr.kua.evadecoration.gui

import kr.kua.evadecoration.util.warnFormat
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.InventoryView

class GuiManager {

    companion object {
        //        lateinit var Instance: GuiManager
        val guiMap = HashMap<InventoryView, GuiInterface>()
    }

    fun onClick(event: InventoryClickEvent) {
        if (event.view in guiMap) guiMap[event.view]!!.onClick(event)
    }

    fun onClose(event: InventoryCloseEvent) {
        if (event.view in guiMap) {
            guiMap[event.view]!!.onClose(event)
            guiMap.remove(event.view)
        }
    }

    fun onDrag(event: InventoryDragEvent) {
        if (event.view in guiMap) {
            event.isCancelled = true
        }
    }

    fun dispose() {
        guiMap.forEach { (view, _) ->
            view.player.sendMessage("관리자에 의해 창이 닫혔습니다.".warnFormat())
            view.close()
        }
    }

}