package space.banterbox.app.core.data.repository

import android.content.Context
import space.banterbox.app.R
import space.banterbox.app.core.domain.model.CountryCodeModel
import space.banterbox.app.core.domain.repository.CountryCodeListRepository
import space.banterbox.app.parseJsonFromString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This stays in memory even after logged in, which is not necessary
 * TODO: To fix this, implement manual singleton pattern and destroy it when not needed.
 */
@Singleton
class DefaultCountryCodeListRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : CountryCodeListRepository {
    /**
     * A backing hot field for [CountryCodeModel] list.
     */
    private val countryCodeModelListFlow: MutableSharedFlow<List<CountryCodeModel>> =
        MutableSharedFlow<List<CountryCodeModel>>(
            replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private var shouldLoadCountryList = true

    override val countryCodeModelListStream: Flow<List<CountryCodeModel>> = countryCodeModelListFlow
        .onStart {
            loadCountryCodeModelList()
        }
        .distinctUntilChanged()

    private suspend fun loadCountryCodeModelList() {
        try {
            if (shouldLoadCountryList) {
                withContext(Dispatchers.IO) {
                    val countryCodeList: ArrayList<CountryCodeModel> = arrayListOf()
                    val countryUnicodeList: ArrayList<String> = ArrayList()

                    val stringResponse =
                        parseJsonFromString(context, R.raw.country_codes)
                    val responseArray = JSONArray(stringResponse)
                    for (i in 0 until responseArray.length()) {
                        val jsonObject = responseArray[i] as JSONObject
                        val name = jsonObject.getString("name")
                        val dialCode = jsonObject.getString("dialcode")
                        val isoCode = jsonObject.getString("isocode")
                        val unicodeArray = jsonObject.getJSONArray("unicode")
                        for (j in 0 until unicodeArray.length()) {
                            val singleUnicode = unicodeArray[j] as String
                            countryUnicodeList.add(singleUnicode)
                        }
                        val countryCodeModel =
                            CountryCodeModel(name, dialCode, isoCode, countryUnicodeList)
                        countryCodeList.add(countryCodeModel)
                    }
                    countryCodeModelListFlow.tryEmit(countryCodeList)

                    Timber.d("Countries: ${countryCodeList.size}")
                }
                shouldLoadCountryList = false
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e.toString())
        }
    }

    companion object {
        private const val TAG = "CountryListRepository"

        @Volatile
        private var INSTANCE: DefaultCountryCodeListRepository? = null

        @Synchronized
        fun getInstance(context: Context): DefaultCountryCodeListRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DefaultCountryCodeListRepository(context).also { INSTANCE = it }
            }
        }

        @Synchronized
        fun destroyInstance() {
            INSTANCE = null
        }

    }
}