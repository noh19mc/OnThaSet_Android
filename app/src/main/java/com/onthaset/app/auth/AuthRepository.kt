package com.onthaset.app.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SignUpResult {
    /** Supabase project has email confirmation OFF — user is signed in immediately. */
    data object SignedIn : SignUpResult
    /** Supabase project has email confirmation ON — user must click the confirmation link. */
    data class NeedsConfirmation(val email: String) : SignUpResult
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: Auth,
) {
    val state: Flow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.RefreshFailure -> AuthState.SignedOut
            is SessionStatus.NotAuthenticated -> AuthState.SignedOut
            is SessionStatus.Authenticated -> {
                val u = status.session.user
                AuthState.SignedIn(userId = u?.id.orEmpty(), email = u?.email)
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String): SignUpResult {
        val trimmed = email.trim()
        auth.signUpWith(Email) {
            this.email = trimmed
            this.password = password
        }
        // Supabase signUpWith doesn't tell us directly whether confirmation is required.
        // If a session was established, the project has confirmation OFF; otherwise we're
        // in the "check your email" flow.
        return if (auth.currentSessionOrNull() != null) SignUpResult.SignedIn
        else SignUpResult.NeedsConfirmation(trimmed)
    }

    suspend fun resendConfirmation(email: String) {
        auth.resendEmail(type = OtpType.Email.SIGNUP, email = email.trim())
    }

    suspend fun resetPassword(email: String) {
        auth.resetPasswordForEmail(email.trim())
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
