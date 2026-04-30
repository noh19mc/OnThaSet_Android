package com.onthaset.app.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthaset.app.auth.AuthRepository
import com.onthaset.app.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportEventUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,
)

@HiltViewModel
class ReportEventViewModel @Inject constructor(
    private val repo: ReportsRepository,
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportEventUiState())
    val state: StateFlow<ReportEventUiState> = _state.asStateFlow()

    fun submit(
        eventId: String,
        eventTitle: String,
        reason: ReportReason?,
        notes: String,
    ) = viewModelScope.launch {
        if (reason == null) {
            _state.value = _state.value.copy(error = "Pick a reason.")
            return@launch
        }
        val signedIn = auth.state.first { it !is AuthState.Loading } as? AuthState.SignedIn
        if (signedIn == null) {
            _state.value = _state.value.copy(error = "Sign in to report.")
            return@launch
        }
        _state.value = _state.value.copy(isSubmitting = true, error = null)
        runCatching {
            repo.report(
                eventId = eventId,
                eventTitle = eventTitle,
                reportedByUserId = signedIn.userId,
                reason = reason,
                notes = notes.trim(),
            )
        }
            .onSuccess { _state.value = ReportEventUiState(submitted = true) }
            .onFailure { _state.value = _state.value.copy(isSubmitting = false, error = it.message ?: "Report failed") }
    }
}
