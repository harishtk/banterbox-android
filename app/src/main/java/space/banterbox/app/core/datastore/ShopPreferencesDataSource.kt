package space.banterbox.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pepul.shops.core.datastore.di.DataStoreType
import com.pepul.shops.core.datastore.di.BanterboxDataStoreType
import space.banterbox.app.core.domain.model.ShopData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopPreferencesDataSource @Inject constructor(
    @DataStoreType(BanterboxDataStoreType.Shop)
    val shopPreferencesStore: DataStore<Preferences>,
) {

    val shopData: Flow<ShopData> = shopPreferencesStore.data
        .catch { t ->
            // datastore data throws an IOException when an error is encountered when reading data
            if (t is IOException) {
                emit(emptyPreferences())
            } else {
                throw t
            }
        }
        .map { preferences ->
            ShopData(
                id = preferences[ShopPreferenceKeys.ShopId] ?: "",
                name = preferences[ShopPreferenceKeys.Name] ?: "",
                thumbnail = preferences[ShopPreferenceKeys.Thumbnail] ?: "",
                category = preferences[ShopPreferenceKeys.Category] ?: "",
                description = preferences[ShopPreferenceKeys.Description] ?: "",
                address = preferences[ShopPreferenceKeys.Address] ?: "",
                image = preferences[ShopPreferenceKeys.Image] ?: "",
            )
        }
        .distinctUntilChanged()

    suspend fun setShopData(shopData: ShopData) {
        shopPreferencesStore.edit { preferences ->
            preferences[ShopPreferenceKeys.ShopId] = shopData.id
            preferences[ShopPreferenceKeys.Name] = shopData.name
            preferences[ShopPreferenceKeys.Thumbnail] = shopData.thumbnail
            preferences[ShopPreferenceKeys.Category] = shopData.category
            preferences[ShopPreferenceKeys.Description] = shopData.description
            preferences[ShopPreferenceKeys.Address] = shopData.address
            preferences[ShopPreferenceKeys.Image] = shopData.image
        }
    }

    suspend fun clearAll() {
        shopPreferencesStore.edit(MutablePreferences::clear)
    }

    object ShopPreferenceKeys {
        val ShopId = stringPreferencesKey("id")
        val Name = stringPreferencesKey("name")
        val Thumbnail = stringPreferencesKey("thumbnail")
        val Category = stringPreferencesKey("category")
        val Description = stringPreferencesKey("description")
        val Address = stringPreferencesKey("address")
        val Image = stringPreferencesKey("image")
    }

}