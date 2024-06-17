package kr.kua.evadecoration.entity

import com.google.gson.annotations.SerializedName

data class UserMagicCosmetic(
    @SerializedName("A") val id: String, // ClothData
    @SerializedName("B") val magicCosmeticColor: String, // MagicCosmetic Color(without #)
    @SerializedName("C") var isDress: Boolean, // 옷 입은지 안입은지
    @SerializedName("D") val acquisitionDate: Long, // 획득 날짜
)