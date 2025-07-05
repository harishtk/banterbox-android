package space.banterbox.app.feature.onboard.presentation.components.forms

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

class BioState(initialValue: String) :
    TextFieldState(validator = ::isValidBio, errorFor = ::bioError) {
    init {
        text = initialValue
    }
}

private fun isValidBio(bio: String): Boolean {
    return if (bio.isNotEmpty()) {
        bio.length <= 280
    } else {
        true
    }
}

private fun bioError(bio: String): String {
    return when {
        bio.length > 280 -> "Bio cannot exceed 280 characters"
        else -> ""
    }
}

val BioStateSaver = textFieldStateSaver(BioState(""))