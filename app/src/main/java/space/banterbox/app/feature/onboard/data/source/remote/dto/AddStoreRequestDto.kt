package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.AddStoreRequest

data class AddStoreRequestDto(
    @SerializedName("tempId")
    val tempId: String,
    @SerializedName("storeName")
    val storeName: String,
    @SerializedName("storeCategory")
    val storeCategory: String,
    @SerializedName("fcm")
    val fcm: String,
)

fun AddStoreRequest.asDto(): AddStoreRequestDto {
    return AddStoreRequestDto(
        tempId = tempId,
        storeName = storeName,
        storeCategory = storeCategory,
        fcm = fcm
    )
}