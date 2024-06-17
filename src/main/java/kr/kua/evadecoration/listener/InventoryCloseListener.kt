package kr.kua.evadecoration.listener

import kr.kua.evadecoration.evaDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener : Listener {

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        evaDecoration.guiManager.onClose(event)
    }
}