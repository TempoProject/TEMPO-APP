package com.tempo.tempoapp.ui.onboarding

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.BuildConfig
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisScreen

object LoginScreen : NavigationDestination {
    override val route: String = "login"
    override val titleRes: Int = R.string.login_screen_title
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController?) {

    val viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val errorString = stringResource(R.string.error_login)
    LaunchedEffect(uiState.value.errorMessage) {
        uiState.value.errorMessage?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorString,
                duration = SnackbarDuration.Long
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.login_screen_title)
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            // Content of the login screen goes here
            if (uiState.value.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = false) {}
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.app_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Text(
                    text = stringResource(R.string.registration_required_message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                )
                TextField(
                    value = uiState.value.userId,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = viewModel::onUserIdChange,
                    label = { Text(stringResource(R.string.insert_id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.value.isLoading,
                    isError = !uiState.value.errorMessage.isNullOrEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.no_account_question),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    BuildConfig.REGISTRATION_URL.toUri()
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.d("LoginScreen", "Error opening URL: ${e.message}")
                            }
                        }
                    )

                    Button(
                        onClick = {
                            viewModel.login {
                                navController?.navigate(
                                    ProphylaxisScreen.route
                                ) {
                                    popUpTo(LoginScreen.route) {
                                        inclusive = true
                                    }
                                }
                            }
                            // Navigate to HomeScreen after login

                        },
                        enabled = uiState.value.userId.isNotBlank() && !uiState.value.isLoading,
                    ) {
                        Text(text = stringResource(R.string.login))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.recover_id),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(16.dp)
                        .clickable {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    BuildConfig.REGISTRATION_URL.toUri()
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.d("LoginScreen", "Error opening URL: ${e.message}")
                            }
                        }
                )
            }

        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = null)
}