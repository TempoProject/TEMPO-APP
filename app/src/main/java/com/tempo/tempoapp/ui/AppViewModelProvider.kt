package com.tempo.tempoapp.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.ui.bleeding.BleedingEventViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            BleedingEventViewModel(tempoApplication().container.bleedingRepository)
        }
    }
}

fun CreationExtras.tempoApplication(): TempoApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TempoApplication)