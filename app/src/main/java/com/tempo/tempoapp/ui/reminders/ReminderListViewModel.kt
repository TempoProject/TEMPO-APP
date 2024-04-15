package com.tempo.tempoapp.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class ReminderListViewModel(
    private val reminderRepository: ReminderRepository,
    private val workManager: WorkManager
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
        workManager.cancelWorkById(item.uuid)
        reminderRepository.deleteItem(item)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ReminderListUiState(
    val reminderList: List<ReminderEvent> = listOf()
)

