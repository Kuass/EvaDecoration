package kr.kua.evadecoration.entity

import com.google.gson.annotations.SerializedName

data class UserBadge(
    @SerializedName("A") val id: String,
    @SerializedName("B") var isDress: Boolean,
    @SerializedName("C") val acquisitionDate: Long
)