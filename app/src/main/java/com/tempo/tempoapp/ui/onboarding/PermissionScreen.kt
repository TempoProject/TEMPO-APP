import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.preferences
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object PermissionScreen : NavigationDestination {
    override val route: String = "permission_screen"
    override val titleRes: Int = R.string.permission_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }

    var missingPermissions by remember { mutableStateOf(emptyList<String>()) }
    var areHealthPermissionsGranted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var permissionAttempts by remember { mutableIntStateOf(0) }
    var healthPermissionAttempts by remember { mutableIntStateOf(0) }

    // Funzione per controllare i permessi
    fun checkPermissions() {
        missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    // Funzione per aprire le impostazioni dell'app
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    val launcherStandardPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            checkPermissions()
            val allGranted = results.values.all { it }

            if (allGranted) {
                // Reset contatore se i permessi sono stati concessi
                permissionAttempts = 0
            } else {
                // Incrementa contatore solo se ci sono stati rifiuti
                permissionAttempts++
                // Mostra dialog solo dopo 2 tentativi
                if (permissionAttempts >= 2) {
                    showSettingsDialog = true
                }
            }
        }

    val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    val launcherHealthPermissions =
        rememberLauncherForActivityResult(requestPermissionActivityContract) { isGranted ->
            val requiredPermissions = setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
            )

            areHealthPermissionsGranted = isGranted.containsAll(requiredPermissions)

            if (!areHealthPermissionsGranted) {
                showSettingsDialog = true
            }
        }

    // Controlla i permessi all'avvio
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    // Naviga quando tutti i permessi sono concessi
    LaunchedEffect(missingPermissions, areHealthPermissionsGranted) {
        if (missingPermissions.isEmpty() && areHealthPermissionsGranted) {
            navController.navigate(HomeDestination.route) {
                popUpTo(PermissionScreen.route) { inclusive = true }

                // Update

                coroutine.launch { context.preferences.setFirstLaunch(false) }

            }
        }
    }

    // Dialog per aprire le impostazioni
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permessi richiesti") },
            text = {
                Text(
                    "Per funzionare correttamente, Tempo ha bisogno di alcuni permessi. " +
                            "Puoi abilitarli manualmente nelle impostazioni dell'app."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        openAppSettings()
                    }
                ) {
                    Text("Apri Impostazioni")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        // Reset contatori per permettere nuovi tentativi
                        permissionAttempts = 0
                        healthPermissionAttempts = 0
                    }
                ) {
                    Text("Riprova")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.permission_screen_title))
                },
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            PermissionsList()
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    coroutine.launch {
                        context.preferences.setFirstLaunch(false)
                        when {
                            missingPermissions.isNotEmpty() -> {
                                launcherStandardPermissions.launch(missingPermissions.toTypedArray())
                            }

                            !areHealthPermissionsGranted -> {
                                launcherHealthPermissions.launch(
                                    setOf(
                                        HealthPermission.getReadPermission(StepsRecord::class),
                                        HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
                                    )
                                )
                            }

                            else -> {
                                // Tutti i permessi sono concessi, naviga alla home

                                // TODO se l'utente è loggato vai alla home, altrimenti al login.

                                navController.navigate("splash_screen") {
                                    popUpTo(PermissionScreen.route) { inclusive = true }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Text("Autorizza e continua")
            }
        }
    }
}

@Composable
fun PermissionsList() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Per funzionare correttamente, Tempo richiede le seguenti autorizzazioni:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PermissionItem(
            image = Icons.Default.LocationOn,
            iconDescription = "Icona localizzazione",
            title = "Localizzazione",
            description = "Ci serve per prendere dati contestuali come le informazioni meteorologiche"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItem(
            image = ImageVector.vectorResource(R.drawable.outline_health_metrics_24),
            iconDescription = "Icona Health Connect",
            title = "Health Connect",
            description = "Per collezionare dati relativi alla salute del paziente"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItem(
            image = ImageVector.vectorResource(R.drawable.outline_bluetooth_24),
            iconDescription = "Icona Bluetooth",
            title = "Bluetooth",
            description = "Per l'utilizzo di dispositivi esterni come Movesense"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItem(
            image = Icons.Default.Notifications,
            iconDescription = "Icona Notifica",
            title = "Notifiche",
            description = "Per ricevere promemoria e notifiche importanti"
        )
    }
}

@Composable
fun PermissionItem(
    image: ImageVector = Icons.Default.Star,
    iconDescription: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = image,
            contentDescription = iconDescription,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            tint = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}