package kr.kua.evadecoration.util

import com.francobm.magicosmetics.api.CosmeticType
import kr.kua.evadecoration.entity.EDCosmeticType
import org.bukkit.ChatColor

class MagicCosmeticUtil {
    companion object {
        fun clothTypeToMagicType(type: EDCosmeticType): CosmeticType = when (type) {
            EDCosmeticType.HAT -> CosmeticType.HAT
            EDCosmeticType.BAG -> CosmeticType.BAG
            EDCosmeticType.WALKING_STICK -> CosmeticType.WALKING_STICK
            EDCosmeticType.BALLOON -> CosmeticType.BALLOON
        }

        fun cmTagToString(tags: List<String>): List<String> {
            val convertedTags = mutableListOf<String>()
            tags.forEach {
                val split = it.split(":")
                if (split.isNotEmpty()) {
                    when (split[0]) {
                        "premium" -> convertedTags.add("${ChatColor.BLUE}◆ 프리미엄 에디션")
                        "notrade" -> convertedTags.add("${ChatColor.RED}◆ 거래 불가능 : ${split[1]}")
                        "limited" -> convertedTags.add("${ChatColor.GOLD}◆ 리미티드 에디션")
                        "season_edition" -> convertedTags.add("${ChatColor.AQUA}◆ 시즌 이벤트 에디션 : ${split[1]}")
                    }
                }
            }
            return convertedTags
        }
    }
}

