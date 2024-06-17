package kr.kua.evadecoration.listener

import kr.kua.evadecoration.evaDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        evaDecoration.guiManager.onClick(event)
    }
}