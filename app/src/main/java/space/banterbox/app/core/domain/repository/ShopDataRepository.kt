package space.banterbox.app.core.domain.repository

import space.banterbox.app.core.domain.model.ShopData
import kotlinx.coroutines.flow.Flow

interface ShopDataRepository {

    val shopData: Flow<ShopData>

    suspend fun setShopData(shopData: ShopData?)
}