package com.tempo.tempoapp.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime

@Composable
fun ProphylaxisItem(item: ProphylaxisResponse, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(id = R.string.prophylaxis_details),
                style = MaterialTheme.typography.titleLarge
            )
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.treatment),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = item.reminderType,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.drug_name),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = item.drugName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.dose_units),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = item.dosage.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.infusion_performed),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(item.responded.mapResponse()),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Text(
                text = if (item.responseDateTime != 0L) item.date.toStringDate() + " " + item.responseDateTime.toStringTime() else item.date.toStringDate(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(alignment = Alignment.End)
            )
        }
    }
}

fun Int.mapResponse(): Int = when (this) {
    0 -> R.string.no
    1 -> R.string.yes
    // TODO handle other cases if needed
    else -> R.string.no
}