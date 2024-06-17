package kr.kua.evadecoration.gui

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

interface GuiInterface : Listener {

    fun render(inv: Inventory)

    @EventHandler
    fun onClick(event: InventoryClickEvent)

    @EventHandler
    fun onClose(event: InventoryCloseEvent)

}