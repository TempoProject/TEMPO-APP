package com.tempo.tempoapp.utils

import AppPreferencesManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExportService(private val context: Context) {

    val preferences = AppPreferencesManager(context)
    suspend fun createTempCsvAndGetUri(
        bleedingEvents: List<BleedingEvent>,
        infusionEvents: List<InfusionEvent>,
        prophylaxisResponses: List<ProphylaxisResponse>
    ): Uri? {
        return try {
            val pid = preferences.userId.first() ?: "unknown_user"

            val fileName = "tempo_app_export_${pid}_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            }.csv"


            val tempFile = File(context.cacheDir, fileName)

            FileWriter(tempFile).use { writer ->

                writeCSVHeader(writer)

                writeBleedingEvents(writer, bleedingEvents)
                writeInfusionEvents(writer, infusionEvents)
                writeProphylaxisResponses(writer, prophylaxisResponses)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun createAndShareCsv(
        uri: Uri,
        context: Context
    ) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Esportazione dati Tempo App")
            putExtra(Intent.EXTRA_SUBJECT, "Dati Tempo App CSV")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Condividi dati CSV")
        context.startActivity(chooser)
    }

    private fun writeCSVHeader(writer: FileWriter) {
        writer.append("Type,ID,Date,Time,Event_Type,Cause,Site,Treatment,Medication_Type,Dose,Dosage_Unit,Lot_Number,Pain_Scale,Reason,Drug_Name,Batch_Number,Reminder_DateTime,Responded,Response_DateTime,Reminder_Type,Postponed_Alarm_ID,Notes\n")
    }

    private fun writeBleedingEvents(writer: FileWriter, events: List<BleedingEvent>) {
        events.forEach { event ->
            writer.append("BLEEDING,")
            writer.append("${event.id},")
            writer.append("${event.date.toStringDate()},")
            writer.append("${event.timestamp.toStringTime()},")
            writer.append("${csvEscape(event.eventType ?: "")},")
            writer.append("${csvEscape(event.bleedingCause)},")
            writer.append("${csvEscape(event.bleedingSite)},")
            writer.append("${csvEscape(event.treatment)},")
            writer.append("${csvEscape(event.medicationType ?: "")},")
            writer.append("${csvEscape(event.dose ?: "")},")
            writer.append("${csvEscape(event.dosageUnit ?: "")},")
            writer.append("${csvEscape(event.lotNumber ?: "")},")
            writer.append("${event.painScale},")
            writer.append(",,,,,,,") // campi vuoti per altri tipi
            writer.append("${csvEscape(event.note ?: "")}\n")
        }
    }

    private fun writeInfusionEvents(writer: FileWriter, events: List<InfusionEvent>) {
        events.forEach { event ->
            writer.append("INFUSION,")
            writer.append("${event.id},")
            writer.append("${event.date.toStringDate()},")
            writer.append("${event.timestamp.toStringTime()},")
            writer.append(",,,,,,,,,") // campi vuoti per bleeding
            writer.append("${csvEscape(event.reason ?: "")},")
            writer.append("${csvEscape(event.drugName ?: "")},")
            writer.append("${csvEscape(event.batchNumber ?: "")},")
            writer.append(",,,,,") // campi vuoti per prophylaxis
            writer.append("${csvEscape(event.note ?: "")}\n")
        }
    }

    private fun writeProphylaxisResponses(
        writer: FileWriter,
        responses: List<ProphylaxisResponse>
    ) {
        responses.forEach { response ->
            writer.append("PROPHYLAXIS,")
            writer.append("${response.id},")
            writer.append("${response.date.toStringDate()},")
            writer.append(",")
            writer.append(",,,,,,,,,,,")
            writer.append("${response.reminderDateTime.toStringTime()},")
            writer.append("${response.responded},")
            writer.append("${if (response.responseDateTime != -1L) response.responseDateTime.toStringTime() else ""},")
            writer.append("${csvEscape(response.reminderType)},")
            writer.append("${response.postponedAlarmId},")
            writer.append("\n")
        }
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}