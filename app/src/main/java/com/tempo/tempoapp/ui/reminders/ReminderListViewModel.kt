package com.tempo.tempoapp.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.utils.AlarmReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class ReminderListViewModel(
    private val reminderRepository: ReminderRepository,
    private val alarmManager: AlarmManager,
    private val context: Context
) : ViewModel() {
    val reminderListUiState: StateFlow<ReminderListUiState> = combine(
        reminderRepository.getAll()
    ) {
        ReminderListUiState(it[0])
    }.stateIn(
        scope = viewModelScope,
        initialValue = ReminderListUiState(),
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
    )

    suspend fun deleteReminder(item: ReminderEvent) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("REMINDER", item)
        intent.putExtra("id", item.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.timestamp.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        alarmManager.cancel(pendingIntent)
        reminderRepository.deleteItem(item)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ReminderListUiState(
    val reminderList: List<ReminderEvent> = listOf()
)

