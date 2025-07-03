package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.AddBankRequest

data class AddBankRequestDto(
    @SerializedName("accountNumber")
    val accountNumber: String,
    @SerializedName("holderName")
    val accountHolderName: String,
    @SerializedName("ifscCode")
    val ifscCode: String,
)

fun AddBankRequest.asDto(): AddBankRequestDto {
    return AddBankRequestDto(
        accountNumber = accountNumber,
        accountHolderName = accountHolderName,
        ifscCode = ifscCode
    )
}