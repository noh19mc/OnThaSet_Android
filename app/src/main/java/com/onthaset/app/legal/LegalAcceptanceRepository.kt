package com.onthaset.app.legal

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.legalDataStore by preferencesDataStore(name = "legal_acceptance")
private val ACCEPTED_KEY = booleanPreferencesKey("accepted_v1")

/**
 * Tracks whether the user has accepted the EVENT LIABILITY NOTICE + Terms / Privacy.
 * Versioned ("_v1") so we can require re-acceptance if the legal text materially changes.
 */
@Singleton
class LegalAcceptanceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val accepted: Flow<Boolean> = context.legalDataStore.data.map { it[ACCEPTED_KEY] == true }

    suspend fun accept() {
        context.legalDataStore.edit { it[ACCEPTED_KEY] = true }
    }
}
