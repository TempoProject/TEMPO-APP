package com.tempo.tempoapp.ui.onboarding

import AppPreferencesManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tempo.tempoapp.utils.CrashlyticsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val preferences: AppPreferencesManager) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState


    fun onUserIdChange(newId: String) {
        _uiState.value = _uiState.value.copy(userId = newId)
    }


    fun login(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                delay(2000) // Simula una chiamata a un backend
                preferences.setLoggedIn(true)
                preferences.setUserId(_uiState.value.userId)
                _uiState.value = _uiState.value.copy(isLoading = false)

                FirebaseCrashlytics.getInstance().setUserId("tempo_user_${_uiState.value.userId}")

                CrashlyticsHelper.logCriticalAction(
                    action = "user_login",
                    success = true,
                    details = "User authenticated successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )

                CrashlyticsHelper.logCriticalAction(
                    action = "user_login",
                    success = false,
                    details = "User authentication failed: ${e.message}"
                )

                return@launch
            }

        }
    }
}

data class LoginUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)