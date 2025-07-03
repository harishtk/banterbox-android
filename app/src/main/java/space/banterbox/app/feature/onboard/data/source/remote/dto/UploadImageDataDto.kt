package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.UploadImageData
import space.banterbox.app.nullAsEmpty

@Deprecated("unused")
data class UploadImageDataDto(
    @SerializedName("fileName")
    val imageName: String?,
)
