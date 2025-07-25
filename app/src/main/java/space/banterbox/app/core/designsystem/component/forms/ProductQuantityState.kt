package space.banterbox.app.core.designsystem.component.forms

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

class ProductQuantityState(quantity: String)
    : TextFieldState() {
        init {
            text = quantity
        }
}

private fun productQuantityValidationError(): String {
    return "Quantity Error"
}

private fun isQuantityValid(quantity: String): Boolean {
    return quantity.isNotBlank()
}

val ProductDiscountStateSaver = textFieldStateSaver(ProductQuantityState(""))