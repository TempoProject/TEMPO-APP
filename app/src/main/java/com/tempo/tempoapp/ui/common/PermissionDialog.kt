package com.tempo.tempoapp.ui.common


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tempo.tempoapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDialog(
    showDialog: Boolean,
    permission: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Autorizzazione richiesta")
            },
            text = {
                Text(text = permission.getDescription(isPermanentlyDeclined))
            },
            confirmButton = {
                Text(
                    text = if (isPermanentlyDeclined) "Apri impostazioni" else "Concedi autorizzazione",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPermanentlyDeclined) {
                                onDismiss()
                                onGoToAppSettings()

                            } else {
                                onDismiss()
                                onOkClick()

                            }
                        }
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                )

            },
            modifier = modifier
        )
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean): String

}

class AlarManagerTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Per funzionare correttamente, l'applicazione richiede l'autorizzazione ad impostare allarmi e promemoria."
        } else
            "Per funzionare correttamente, l'applicazione richiede l'autorizzazione ad impostare allarmi e promemoria."
    }
}

class NotificationTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Per funzionare correttamente, l'applicazione richiede l'autorizzazione ad inviare notifiche push."
        } else
            "Per funzionare correttamente, l'applicazione richiede l'autorizzazione ad inviare notifiche push."
    }
}

class BluetoothTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "È necessario concedere l'accesso al bluetooth per collegare il dispositivo Movesense.\nApri le impostazioni e concedi l'accesso ai dispositivi nelle vicinanze."
        } else
            "È necessario concedere l'accesso al bluetooth per collegare il dispositivo Movesense."
    }
}

class BluetoothLegacyTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "È necessario concedere l'accesso alla posizione del dispositivo per collegare il dispositivo Movesense.\nApri le impostazioni e concedi l'accesso ai alla posizione."
        } else
            "È necessario concedere l'accesso al bluetooth per collegare il dispositivo Movesense."
    }
}