package com.tempo.tempoapp.ui.onboarding

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.navigation.NavigationDestination

object LoginScreen : NavigationDestination {
    override val route: String = "login"
    override val titleRes: Int = R.string.login_screen_title
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController?) {

    val viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()

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
            }
        ) { padding ->
            // Content of the login screen goes here
            if (uiState.value.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        //.background(Color.Black.copy(alpha = 0.3f)) // semi-trasparente
                        .clickable(enabled = false) {} // blocca interazioni sotto
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
                    text = "Tempo app aiuta le persone con emofilia in profilassi a tener traccia dei trattamenti e degli eventi rilevanti.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Text(
                    text = "Per funzionare correttamente è necessario registrarsi e inserire l'ID fornito.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                )
                TextField(
                    value = uiState.value.userId,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onValueChange = viewModel::onUserIdChange,
                    label = { Text(stringResource(R.string.insert_id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.value.isLoading,
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
                        text = "Non hai un account?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            // TODO: Navigate to registration. Add Link to website
                        }
                    )

                    Button(
                        onClick = {
                            // TODO: Handle login
                            viewModel.login {
                                navController?.navigate(
                                    HomeDestination.route
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
                    text = "Password dimenticata?",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(16.dp)
                        .clickable {
                            // TODO: Navigate to password recovery
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