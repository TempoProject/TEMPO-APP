package com.tempo.tempoapp.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.ui.common.BleedingItem
import com.tempo.tempoapp.ui.common.InfusionItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Long.toStringDate(): String =
    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(this))

fun Long.toStringTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

@Composable
fun Loading(){
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
            text = "Operazione in corso",
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
        )
    }
}
@Composable
fun <T> ItemCount(count: T, @DrawableRes iconId: Int, modifier: Modifier = Modifier) {
    Column{
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium)))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

        }
    }
}

@Composable
fun HomeBody(
    bleedingEventList: List<BleedingEvent>,
    infusionEventList: List<InfusionEvent>,
    stepsCount: Int,
    modifier: Modifier = Modifier,
    onInfusionItemClick: (Int) -> Unit,
    onBleedingItemClick: (Int) -> Unit
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
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
            ItemCount(
                count = infusionEventList.count(),
                iconId = R.drawable.baseline_medication_24,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
            ItemCount(
                count = stepsCount,
                iconId = R.drawable.baseline_directions_walk_24,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
        EventsList(
            bleedingEventList,
            infusionEventList,
            onInfusionItemClick = { onInfusionItemClick(it.id) },
            onBleedingItemClick = { onBleedingItemClick(it.id) },
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
        )
    }
}

@Composable
fun EventsList(
    bleedingEventList: List<BleedingEvent>,
    infusionEventList: List<InfusionEvent>,
    onInfusionItemClick: (InfusionEvent) -> Unit,
    onBleedingItemClick: (BleedingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(infusionEventList) {
            InfusionItem(item = it, modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .clickable { onInfusionItemClick(it) })
        }
        items(bleedingEventList) {
            BleedingItem(
                item = it,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onBleedingItemClick(it) }
            )
        }
    }
}