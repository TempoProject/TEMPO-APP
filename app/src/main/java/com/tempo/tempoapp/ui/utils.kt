package com.tempo.tempoapp.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.ui.common.BleedingItem
import com.tempo.tempoapp.ui.common.InfusionItem
import com.tempo.tempoapp.ui.common.ProphylaxisItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Converts a timestamp to a date string in the format "dd-MM-yyyy".
 *
 * @receiver The timestamp.
 * @return The formatted date string.
 */
fun Long.toStringDate(): String =
    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(this))

/**
 * Converts a timestamp to a time string in the format "HH:mm".
 *
 * @receiver The timestamp.
 * @return The formatted time string.
 */
fun Long.toStringTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

/**
 * Composable function representing a loading indicator.
 */
@Composable
fun Loading() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = stringResource(id = R.string.loading),
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
        )
    }
}

/**
 * Composable function representing an item count.
 *
 * @param count The count value.
 * @param iconId The icon resource ID.
 * @param modifier The modifier for the item count.
 */
@Composable
fun <T> ItemCount(
    count: T,
    @DrawableRes iconId: Int,
    @StringRes stringId: Int,
    modifier: Modifier = Modifier,
    @StringRes message: Int = R.string.exit_confirmation_message
) {
    var showDialog by remember { mutableStateOf(false) }


    Column {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium)))
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    showDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(stringId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        if (showDialog)
            InformationDialog(
                onConfirm = { showDialog = false },
                onDismiss = { showDialog = false },
                cancelEnabled = false,
                icon = iconId,
                title = stringId,
                message = message,
                confirm = R.string.confirm,
            )
    }
}

/**
 * Composable function representing the body of the home screen.
 *
 * @param bleedingEventList The list of bleeding events.
 * @param infusionEventList The list of infusion events.
 * @param stepsCount The count of steps.
 * @param modifier The modifier for the home body.
 * @param onInfusionItemClick Callback for when an infusion item is clicked.
 * @param onBleedingItemClick Callback for when a bleeding item is clicked.
 */
@Composable
fun HomeBody(
    bleedingEventList: List<BleedingEvent>,
    infusionEventList: List<InfusionEvent>,
    stepsCount: Int,
    combinedEvent: List<BodyEvent>,
    modifier: Modifier = Modifier,
    onInfusionItemClick: (Int) -> Unit,
    onBleedingItemClick: (Int) -> Unit,
    onProphylaxisItemClick: (Int) -> Unit
) {

    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemCount(
                count = bleedingEventList.count(),
                iconId = R.drawable.baseline_bloodtype_24,
                stringId = R.string.event,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                message = R.string.event_count_details_message
            )
            ItemCount(
                count = infusionEventList.count(),
                iconId = R.drawable.baseline_medication_24,
                stringId = R.string.infusion,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                message = R.string.infusion_count_details_message
            )
            ItemCount(
                count = stepsCount,
                iconId = R.drawable.baseline_directions_walk_24,
                stringId = R.string.steps,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                message = R.string.steps_count_details_message
            )
        }
        Spacer(Modifier.padding(10.dp))
        EventsList(
            combinedEvent,
            onInfusionItemClick = { onInfusionItemClick(it.id) },
            onBleedingItemClick = { onBleedingItemClick(it.id) },
            onProphylaxisItemClick = { onProphylaxisItemClick(it.id) }
        )
    }
}

@Composable
fun EventsList(
    combinedEvents: List<BodyEvent>,
    onInfusionItemClick: (InfusionEvent) -> Unit,
    onBleedingItemClick: (BleedingEvent) -> Unit,
    onProphylaxisItemClick: (ProphylaxisResponse) -> Unit = {}
) {
    LazyColumn {
        items(combinedEvents) { event ->
            when (event) {
                is BodyEvent.Bleeding -> BleedingItem(
                    event.event, modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_small))
                        .clickable { onBleedingItemClick(event.event) })

                is BodyEvent.Infusion -> InfusionItem(
                    event.event, modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_small))
                        .clickable { onInfusionItemClick(event.event) })

                is BodyEvent.Prophylaxis -> ProphylaxisItem(
                    event.event, modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_small))
                        .clickable { onProphylaxisItemClick(event.event) }
                )
            }
        }
    }
}

sealed class BodyEvent(val dateTime: Long) {
    data class Bleeding(val event: BleedingEvent) : BodyEvent(event.timestamp)
    data class Infusion(val event: InfusionEvent) : BodyEvent(event.timestamp)
    data class Prophylaxis(val event: ProphylaxisResponse) : BodyEvent(event.reminderDateTime)
}

enum class DosageUnit {
    MG_KG, // mg/kg
    IU     // International Units
}

fun filterDoseInput(input: String): String {

    val filtered = input.filter { it.isDigit() || it == ',' }

    val commaCount = filtered.count { it == ',' }
    if (commaCount > 1) {

        val firstCommaIndex = filtered.indexOf(',')
        return filtered.substring(0, firstCommaIndex + 1) +
                filtered.substring(firstCommaIndex + 1).replace(",", "")
    }

    if (filtered.startsWith(",")) {
        return "0$filtered"
    }

    return filtered
}

@Composable
fun InformationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelEnabled: Boolean = true,
    @DrawableRes icon: Int = -1,
    @StringRes title: Int = R.string.confirm,
    @StringRes message: Int = R.string.exit_confirmation_message,
    @StringRes confirm: Int = R.string.exit,
    @StringRes cancel: Int = R.string.cancel

) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (icon != -1) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        title = {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(

                    contentColor = if (icon != -1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(confirm))
            }
        },
        dismissButton = {
            if (cancelEnabled)
                TextButton(onClick = onDismiss) {
                    Text(stringResource(cancel))
                }
        }
    )
}

fun String.toStringDosage(): String {
    return when (this) {
        DosageUnit.MG_KG.name -> "mg/kg"
        DosageUnit.IU.name -> "IU"
        else -> ""
    }
}