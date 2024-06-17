package kr.kua.evadecoration.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

operator fun Material.times(i: Int): ItemStack {
    return ItemStack(this, i)
}