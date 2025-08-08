package com.tempo.tempoapp.ui.onboarding

import AppPreferencesManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tempo.tempoapp.utils.ApiLogin
import com.tempo.tempoapp.utils.CrashlyticsHelper
import com.tempo.tempoapp.utils.PatientVerify
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
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val response = ApiLogin.retrofitService.verifyPatient(
                    PatientVerify(_uiState.value.userId.toInt())
                )

                println("HTTP Code: ${response.code()}")
                println("Is successful: ${response.isSuccessful}")

                when (response.code()) {
                    201 -> {
                        preferences.setLoggedIn(true)
                        preferences.setUserId(_uiState.value.userId)
                        _uiState.value = _uiState.value.copy(isLoading = false)

                        FirebaseCrashlytics.getInstance()
                            .setUserId("tempo_user_${_uiState.value.userId}")

                        CrashlyticsHelper.logCriticalAction(
                            action = "user_login",
                            success = true,
                            details = "User authenticated successfully"
                        )

                        onSuccess()
                    }

                    401 -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Unauthorized"
                        )
                    }

                    404 -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Patient ID not found"
                        )
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Errore di verifica: ${response.code()}"
                        )
                    }
                }

                // Log per tutti i casi
                CrashlyticsHelper.logCriticalAction(
                    action = "user_login",
                    success = response.code() == 201,
                    details = "HTTP ${response.code()}: ${if (response.code() == 201) "Verified" else uiState.value.errorMessage}"
                )

            } catch (e: Exception) {
                println("Exception: ${e.message}")
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Errore di connessione: ${e.message}"
                )

                CrashlyticsHelper.logCriticalAction(
                    action = "user_login",
                    success = false,
                    details = "Network error: ${e.message}"
                )
            }
        }
    }
}

data class LoginUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)