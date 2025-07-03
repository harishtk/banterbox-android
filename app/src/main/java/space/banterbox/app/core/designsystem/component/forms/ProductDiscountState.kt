package space.banterbox.app.core.designsystem.component.forms

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

class ProductDiscountState(discount: String)
    : TextFieldState() {
        init {
            text = discount
        }
}

private fun productDiscountValidationError(): String {
    return "Discount Error"
}

private fun isValidDiscount(discount: String): Boolean {
    return discount.isNotBlank()
}

val ProductDiscountSaver = textFieldStateSaver(ProductDiscountState(""))