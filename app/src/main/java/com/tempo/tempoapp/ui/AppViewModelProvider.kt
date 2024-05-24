package com.tempo.tempoapp.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.movesense.AndroidBluetoothController
import com.tempo.tempoapp.ui.bleeding.BleedingDetailsViewModel
import com.tempo.tempoapp.ui.bleeding.BleedingEditViewModel
import com.tempo.tempoapp.ui.bleeding.BleedingEntryViewModel
import com.tempo.tempoapp.ui.history.HistoryViewModel
import com.tempo.tempoapp.ui.home.HomeViewModel
import com.tempo.tempoapp.ui.infusion.InfusionDetailsViewModel
import com.tempo.tempoapp.ui.infusion.InfusionEditViewModel
import com.tempo.tempoapp.ui.infusion.InfusionEntryViewModel
import com.tempo.tempoapp.ui.movesense.MovesenseViewModel
import com.tempo.tempoapp.ui.movesense.ScanDevicesViewModel
import com.tempo.tempoapp.ui.reminders.ReminderListViewModel
import com.tempo.tempoapp.ui.reminders.ReminderViewModel

/**
 * AppViewModelProvider is an object responsible for providing ViewModel instances using Jetpack ViewModelProvider.
 * It encapsulates the initialization logic for various ViewModel classes used within the Tempo app.
 */
object AppViewModelProvider {

    /**
     * Factory is a ViewModelProvider.Factory instance used for creating ViewModel instances.
     */
    val Factory = viewModelFactory {
        initializer {
            BleedingEntryViewModel(tempoApplication().container.bleedingRepository)
        }
        initializer {
            HomeViewModel(
                tempoApplication().container.bleedingRepository,
                tempoApplication().container.infusionRepository,
                tempoApplication().container.stepsRecordRepository,
                tempoApplication().container.movesenseRepository,
                tempoApplication().healthConnectManager,
            )
        }
        initializer {
            BleedingDetailsViewModel(
                this.createSavedStateHandle(),
                tempoApplication().container.bleedingRepository
            )
        }
        initializer {
            BleedingEditViewModel(
                this.createSavedStateHandle(),
                tempoApplication().container.bleedingRepository
            )
        }

        initializer {
            InfusionEntryViewModel(
                tempoApplication().container.infusionRepository
            )
        }
        initializer {
            InfusionDetailsViewModel(
                this.createSavedStateHandle(),
                tempoApplication().container.infusionRepository
            )
        }

        initializer {
            InfusionEditViewModel(
                this.createSavedStateHandle(),
                tempoApplication().container.infusionRepository
            )
        }
        initializer {
            HistoryViewModel(
                tempoApplication().container.bleedingRepository,
                tempoApplication().container.infusionRepository,
                tempoApplication().container.stepsRecordRepository
            )
        }
        initializer {
            ReminderViewModel(
                tempoApplication().container.reminderRepository,
                tempoApplication().applicationContext
            )
        }

        initializer {
            ReminderListViewModel(
                tempoApplication().container.reminderRepository,
                tempoApplication().applicationContext
            )
        }
        initializer {
            ScanDevicesViewModel(
                tempoApplication().container.movesenseRepository,
                AndroidBluetoothController(tempoApplication().applicationContext),
            )
        }
        initializer {
            MovesenseViewModel(
                tempoApplication().container.movesenseRepository
            )
        }
    }
}

/**
 * Extension function to retrieve TempoApplication instance from CreationExtras.
 *
 * @return TempoApplication instance.
 */
fun CreationExtras.tempoApplication(): TempoApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TempoApplication)