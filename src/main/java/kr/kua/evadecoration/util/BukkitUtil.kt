package kr.kua.evadecoration.util

import org.bukkit.entity.Player

fun Player.isInventoryFull(): Boolean {
    return this.inventory.firstEmpty() == -1
}