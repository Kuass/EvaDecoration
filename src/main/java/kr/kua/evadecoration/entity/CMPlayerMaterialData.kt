package kr.kua.evadecoration.entity

import com.google.gson.annotations.SerializedName

@Deprecated("Virtual goods will be removed due to unused", level = DeprecationLevel.WARNING)
data class CMPlayerMaterialData(
    @SerializedName("A") var typeCan: Long,
    @SerializedName("B") var typePaid: Long,
    @SerializedName("C") var typeAchieve: Long,
)