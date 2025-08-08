package com.tempo.tempoapp.ui.onboarding

import PermissionScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.navigation.NavigationDestination


object WelcomeScreen : NavigationDestination {
    override val route: String = "welcome"
    override val titleRes: Int = R.string.welcome_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavController?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.welcome_screen_title)
                    )
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.permissions_intro_message),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController?.navigate(PermissionScreen.route) {
                        popUpTo(WelcomeScreen.route) {
                            inclusive = true
                        }
                    }
                    /*
                    navController?.navigate("splash_screen") {
                        popUpTo(WelcomeScreen.route) {
                            inclusive = true
                        }
                    }*/
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.continue_action))
            }
        }
    }
}


@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(navController = null)
}
