package com.tempo.tempoapp.ui.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.repository.ExportRepository
import com.tempo.tempoapp.utils.CsvExportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExportViewModel(
    private val exportRepository: ExportRepository,
    private val csvExportService: CsvExportService
) : ViewModel() {

    private val _exportState = MutableStateFlow(ExportUiState())
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    fun exportAndShareData(activityContext: Context) {
        viewModelScope.launch {
            _exportState.value = _exportState.value.copy(isExporting = true, error = null)

            try {
                val bleedingEvents = exportRepository.getAllBleedingEvents()
                val infusionEvents = exportRepository.getAllInfusionEvents()
                val prophylaxisResponses = exportRepository.getAllProphylaxisResponses()

                val uri = csvExportService.createTempCsvAndGetUri(
                    bleedingEvents,
                    infusionEvents,
                    prophylaxisResponses
                )

                if (uri != null) {
                    _exportState.value = _exportState.value.copy(
                        isExporting = false,
                        totalRecords = bleedingEvents.size + infusionEvents.size + prophylaxisResponses.size,
                        success = true
                    )

                    csvExportService.createAndShareCsv(uri, activityContext)

                } else {
                    _exportState.value = _exportState.value.copy(
                        isExporting = false,
                        error = activityContext.getString(
                            R.string.export_error_csv_creation
                        )
                    )
                }
            } catch (e: Exception) {
                _exportState.value = _exportState.value.copy(
                    isExporting = false,
                    error = "${activityContext.getString(R.string.export_error_generic)}: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportUiState()
    }
}

data class ExportUiState(
    val isExporting: Boolean = false,
    val totalRecords: Int = 0,
    val success: Boolean = false,
    val error: String? = null
)