package com.onthaset.app.billing

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.postCreditsDataStore by preferencesDataStore(name = "post_credits")
private val CREDITS_KEY = intPreferencesKey("single_post_credits")

/**
 * Tracks single-post credits the user has purchased via the $0.99 IAP. Each successful
 * purchase grants one credit (consumed by Play immediately so it can be re-bought).
 * Each event-post call decrements one credit. Subscription-active users bypass this
 * counter entirely.
 *
 * Stored in DataStore rather than Supabase so it survives sign-out (the iOS single-post
 * flow says "No account needed" — we honor that by keeping credits device-local).
 */
@Singleton
class PostCreditsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val credits: Flow<Int> = context.postCreditsDataStore.data.map { it[CREDITS_KEY] ?: 0 }

    suspend fun count(): Int = credits.first()

    suspend fun grant(count: Int = 1) {
        context.postCreditsDataStore.edit { prefs ->
            val current = prefs[CREDITS_KEY] ?: 0
            prefs[CREDITS_KEY] = current + count
        }
    }

    /** Returns true if a credit was consumed (count > 0 before the call). */
    suspend fun consumeOne(): Boolean {
        var consumed = false
        context.postCreditsDataStore.edit { prefs ->
            val current = prefs[CREDITS_KEY] ?: 0
            if (current > 0) {
                prefs[CREDITS_KEY] = current - 1
                consumed = true
            }
        }
        return consumed
    }
}
