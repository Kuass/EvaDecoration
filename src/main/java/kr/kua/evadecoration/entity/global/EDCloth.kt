package kr.kua.evadecoration.entity.global

import kr.kua.evadecoration.entity.EDCosmeticType
import org.bukkit.inventory.ItemStack

data class EDCloth(
    val id: String, // CMDataName
    val name: String,
    val description: String,
    val cosmeticId: String, // MagicCosmetic ID
    val edCosmeticType: EDCosmeticType,
    val cmTag: List<String>?,
    val magicCosmeticComponent: ItemStack,
    val possibleColor: Boolean,
    val possibleDisassemble: Boolean,
    val disassembleItems: List<String>?,
)