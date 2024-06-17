package kr.kua.evadecoration.entity.global

import kr.kua.evadecoration.entity.EDPlayerDataSet
import org.bukkit.entity.Player

data class EDPlayer(
    val player: Player,
    var clothes: EDPlayerDataSet?,
    val dbUniqueId: Long?
)