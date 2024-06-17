package kr.kua.evadecoration.entity

import com.google.gson.annotations.SerializedName

// DB 저장시에만 사용
data class EDPlayerDataSet(
    @SerializedName("playerData") var magicCosmeticClothes: MutableList<UserMagicCosmetic>?,
    var badges: MutableList<UserBadge>?,
    val clothMaterial: CMPlayerMaterialData?,
) {
    constructor(magicCosmeticClothes: MutableList<UserMagicCosmetic>, badge: MutableList<UserBadge>) : this(magicCosmeticClothes, badge, null)
}