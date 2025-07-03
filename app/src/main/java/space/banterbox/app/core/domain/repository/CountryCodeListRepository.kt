package space.banterbox.app.core.domain.repository

import space.banterbox.app.core.domain.model.CountryCodeModel
import kotlinx.coroutines.flow.Flow

interface CountryCodeListRepository {
    val countryCodeModelListStream: Flow<List<CountryCodeModel>>
}