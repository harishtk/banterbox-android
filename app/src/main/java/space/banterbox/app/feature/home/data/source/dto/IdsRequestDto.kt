package space.banterbox.app.feature.home.data.source.dto

import com.google.gson.annotations.SerializedName

data class IdsRequestDto(
    @SerializedName("ids")
    val ids: List<String>
)
