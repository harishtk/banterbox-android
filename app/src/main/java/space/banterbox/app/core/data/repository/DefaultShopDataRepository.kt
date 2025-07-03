package space.banterbox.app.core.data.repository

import com.pepul.shops.core.common.concurrent.AiaDispatchers
import com.pepul.shops.core.common.concurrent.Dispatcher
import space.banterbox.app.core.datastore.ShopPreferencesDataSource
import space.banterbox.app.core.domain.model.ShopData
import space.banterbox.app.core.domain.repository.ShopDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultShopDataRepository @Inject constructor(
    private val dataStore: ShopPreferencesDataSource,
    @Dispatcher(AiaDispatchers.Io)
    private val dispatcher: CoroutineDispatcher
) : ShopDataRepository {

    override val shopData: Flow<ShopData> =
        dataStore.shopData

    override suspend fun setShopData(shopData: ShopData?) = withContext(dispatcher) {
        if (shopData == null) {
            dataStore.clearAll()
        } else {
            dataStore.setShopData(shopData)
        }
    }


}