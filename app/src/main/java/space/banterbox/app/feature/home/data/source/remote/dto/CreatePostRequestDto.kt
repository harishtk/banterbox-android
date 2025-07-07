package space.banterbox.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class CreatePostRequestDto(
    @SerializedName("content")
    val content: String
)
