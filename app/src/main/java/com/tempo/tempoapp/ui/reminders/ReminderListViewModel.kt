package com.tempo.tempoapp.ui.reminders

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel responsible for managing the reminder list UI state and operations.
 *
 * @property reminderRepository Repository for accessing reminder data.
 * @property context Context required for accessing content resolver.
 */
class ReminderListViewModel(
    private val reminderRepository: ReminderRepository,
    private val context: Context
) : ViewModel() {
    // StateFlow representing the UI state of the reminder list
    val reminderListUiState: StateFlow<ReminderListUiState> = combine(
        reminderRepository.getAll()
    ) {
        ReminderListUiState(it[0])
    }.stateIn(
        scope = viewModelScope,
        initialValue = ReminderListUiState(),
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
    )

    /**
     * Suspended function to delete a reminder.
     *
     * @param item ReminderEvent to be deleted.
     */
    suspend fun deleteReminder(item: ReminderEvent) {
        val contentResolver = context.contentResolver
        val deleteUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, item.idCalendar)
        val rows: Int = contentResolver.delete(deleteUri, null, null)
        Log.d(javaClass.simpleName, "Event $rows deleted")
        reminderRepository.deleteItem(item)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Represents the UI state of the reminder list.
 *
 * @property reminderList List of ReminderEvent objects.
 */
data class ReminderListUiState(
    val reminderList: List<ReminderEvent> = listOf()
)

