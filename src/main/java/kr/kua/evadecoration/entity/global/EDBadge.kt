package kr.kua.evadecoration.entity.global

import kr.kua.evadecoration.entity.BadgePosition
import kr.kua.evadecoration.util.times
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class EDBadge(
    val id: String,
    val name: String,
    val description: String,
    val code: String,
    val position: BadgePosition,
    val possibleDisassemble: Boolean,
    val disassembleItems: List<String>?,
    val nameColor: String?,
    val autoPermission: String?,
) {
    companion object {
        fun EDBadge.toItemStack(): ItemStack {
            val badge = this
            return (Material.PAPER * 1).apply { ->
                itemMeta = itemMeta.apply {
                    setDisplayName("${ChatColor.RESET}${badge.code}")
                    if (badge.description.isNotEmpty()) {
                        val lores = mutableListOf<String>()
                        badge.description.split("\n").forEach { lores.add(it) }
                        lores.add("${ChatColor.GRAY}분해 : ${if (badge.possibleDisassemble) "가능" else "불가"}")
//                        if (item.isDress) loreList.add("${ChatColor.GOLD}>> 착용 중")

                        lore = lores
                    }
                }
            }
        }
    }
}