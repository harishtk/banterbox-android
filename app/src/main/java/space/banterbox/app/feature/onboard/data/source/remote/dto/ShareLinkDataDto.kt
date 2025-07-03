package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.ShareLinkData

data class ShareLinkDataDto(
    @SerializedName("shortLink")
    val shortLink: String
)

fun ShareLinkDataDto.toShareLinkData(): ShareLinkData {
    return ShareLinkData(
        shortLink = shortLink
    )
}

