package com.onthaset.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
    /** Email address awaiting confirmation. Non-null = show "check your email" UI. */
    val pendingConfirmation: String? = null,
    val resendInfo: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = repo.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState.Loading,
    )

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    fun clearError() = _form.update { it.copy(error = null) }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        if (!validate(email, password)) return@launch
        _form.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.signIn(email, password) }
            .onFailure { e -> _form.update { it.copy(isLoading = false, error = e.message ?: "Sign in failed") } }
            .onSuccess { _form.update { it.copy(isLoading = false) } }
    }

    fun signUp(email: String, password: String, confirm: String) = viewModelScope.launch {
        if (!validate(email, password)) return@launch
        if (password != confirm) {
            _form.update { it.copy(error = "Passwords do not match.") }
            return@launch
        }
        if (password.length < 6) {
            _form.update { it.copy(error = "Password must be at least 6 characters.") }
            return@launch
        }
        _form.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.signUp(email, password) }
            .onFailure { e -> _form.update { it.copy(isLoading = false, error = e.message ?: "Sign up failed") } }
            .onSuccess { result ->
                _form.update {
                    when (result) {
                        SignUpResult.SignedIn -> it.copy(isLoading = false)
                        is SignUpResult.NeedsConfirmation ->
                            it.copy(isLoading = false, pendingConfirmation = result.email)
                    }
                }
            }
    }

    fun resendConfirmation() = viewModelScope.launch {
        val target = _form.value.pendingConfirmation ?: return@launch
        _form.update { it.copy(isLoading = true, error = null, resendInfo = null) }
        runCatching { repo.resendConfirmation(target) }
            .onFailure { e ->
                _form.update { it.copy(isLoading = false, error = e.message ?: "Couldn't resend") }
            }
            .onSuccess {
                // Supabase's resend endpoint returns 200 OK even when rate-limited (free tier
                // is ~2 confirmation emails per hour, project-wide), so we can't promise
                // delivery — surface the caveat in the message instead of claiming success.
                _form.update {
                    it.copy(
                        isLoading = false,
                        resendInfo = "If your account still needs confirming, a new email is on its way to $target. " +
                            "Check spam too. Supabase rate-limits these emails to a few per hour — if nothing arrives, wait a bit and try again.",
                    )
                }
            }
    }

    fun acknowledgeConfirmation() = _form.update {
        it.copy(pendingConfirmation = null, resendInfo = null)
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _form.update { it.copy(error = "Enter your email above first.") }
            return@launch
        }
        _form.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.resetPassword(email) }
            .onFailure { e -> _form.update { it.copy(isLoading = false, error = e.message ?: "Reset failed") } }
            .onSuccess { _form.update { it.copy(isLoading = false, resetEmailSent = true) } }
    }

    fun acknowledgeResetSent() = _form.update { it.copy(resetEmailSent = false) }

    fun signOut() = viewModelScope.launch { runCatching { repo.signOut() } }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _form.update { it.copy(error = "Please enter your email and password.") }
            return false
        }
        return true
    }
}
