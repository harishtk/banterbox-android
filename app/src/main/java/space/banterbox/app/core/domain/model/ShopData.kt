package space.banterbox.app.core.domain.model

data class ShopData(
    val id: String,
    val name: String,
    val thumbnail: String,
    val category: String,
    val description: String,
    val address: String,
    val image: String,
) {
    companion object {
        fun sample() = ShopData(
            id = "0",
            name = "Sarathas Clothing",
            thumbnail = "",
            category = "Textile & Cloting",
            description = "Sarees, Shirts, Pants",
            address = "600097",
            image = "",
        )
    }
}
