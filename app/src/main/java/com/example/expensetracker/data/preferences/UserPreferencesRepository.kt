package com.example.expensetracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetracker.common.CurrencyFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val lastUsedPaymentMethodIdKey = longPreferencesKey("last_used_payment_method_id")
    private val defaultCurrencyCodeKey = stringPreferencesKey("default_currency_code")

    val lastUsedPaymentMethodId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[lastUsedPaymentMethodIdKey]
    }

    val defaultCurrencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[defaultCurrencyCodeKey] ?: CurrencyFormatter.DEFAULT_CURRENCY_CODE
    }

    suspend fun setLastUsedPaymentMethodId(paymentMethodId: Long) {
        context.dataStore.edit { prefs ->
            prefs[lastUsedPaymentMethodIdKey] = paymentMethodId
        }
    }

    suspend fun clearLastUsedPaymentMethodId() {
        context.dataStore.edit { prefs ->
            prefs.remove(lastUsedPaymentMethodIdKey)
        }
    }

    suspend fun setDefaultCurrencyCode(currencyCode: String) {
        context.dataStore.edit { prefs ->
            prefs[defaultCurrencyCodeKey] = currencyCode
        }
    }
}
