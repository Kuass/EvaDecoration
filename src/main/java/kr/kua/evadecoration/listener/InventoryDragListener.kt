package kr.kua.evadecoration.listener

import kr.kua.evadecoration.evaDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryDragEvent

class InventoryDragListener : Listener {

    @EventHandler
    fun onClick(event: InventoryDragEvent) {
        evaDecoration.guiManager.onDrag(event)
    }
}