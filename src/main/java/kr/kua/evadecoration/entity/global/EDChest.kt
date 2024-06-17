package kr.kua.evadecoration.entity.global

import kr.kua.evadecoration.util.times
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

data class EDChest(
    val id: String,
    val name: String,
    val description: String?,
    val openPrice: Double,
    val clothIndex: List<ChestItem>,
) {
    companion object {
        fun EDChest.toItemStack(): ItemStack {
            val chest = this
            return (Material.CHEST * 1).apply { ->
                itemMeta = itemMeta.apply {
                    setDisplayName(chest.name)
                    if (!chest.description.isNullOrEmpty()) {
                        val lores = mutableListOf<String>()
                        lores.add("${ChatColor.WHITE}상자 오픈 비용: ${chest.openPrice}캔")
                        chest.description.split("\n").forEach { lores.add(it) }

                        lore = lores
                    } else {
                        lore = mutableListOf("${ChatColor.WHITE}상자 오픈 비용: ${chest.openPrice}캔")
                    }
                    addEnchant(Enchantment.DURABILITY, 0, true)
                    addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS)
                    addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
        }
    }

    fun openChest(): ChestItem {
        // The chance of ChestItem can be set from 0.0 to 100.0, and the total will be 100. Use the probability to pick one ChestItem
        val totalChance = clothIndex.map { it.chance }.sum()
        var randomChance = Math.random() * totalChance
        for (chestItem in clothIndex) {
            randomChance -= chestItem.chance
            if (randomChance <= 0) return chestItem
        }
        return clothIndex[0]
    }
}

data class ChestItem(
    val cmCosmeticId: String,
    val chance: Float,
)