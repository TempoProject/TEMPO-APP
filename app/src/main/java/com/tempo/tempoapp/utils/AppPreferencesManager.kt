import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.ui.onboarding.RecurrenceUnit
import com.tempo.tempoapp.ui.onboarding.SchedulingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferencesManager(private val context: Context) {

    companion object {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val ACTIVE_PROPHYLAXIS = booleanPreferencesKey("active_prophylaxis")
        val SCHEDULING_MODE = stringPreferencesKey("scheduling_mode")
        val SELECTED_DAYS = stringPreferencesKey("selected_days")
        val RECURRENCE_INTERVAL = intPreferencesKey("recurrence_interval")
        val RECURRENCE_UNIT = stringPreferencesKey("recurrence_unit")
        val START_DATE = stringPreferencesKey("start_date")
        val HOUR = intPreferencesKey("hour")
        val MINUTE = intPreferencesKey("minute")
        val DRUG_NAME = stringPreferencesKey("drug_name")
        val DOSAGE = stringPreferencesKey("dosage")
        val PROPHYLAXIS_DOSAGE_UNIT = stringPreferencesKey("prophylaxis_dosage_unit")
        val DRUG_NAME_EXTRA =
            stringPreferencesKey("drug_name_extra")

    }


    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_LOGGED_IN] == true }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
        }
    }

    val userId: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_ID] }

    suspend fun setUserId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = id
        }
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] != false
        }

    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = isFirstLaunch
        }
    }

    val isActiveProphylaxis: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ACTIVE_PROPHYLAXIS] == true }

    suspend fun setActiveProphylaxis(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_PROPHYLAXIS] = active
        }
    }

    suspend fun saveProphylaxisConfig(
        schedulingMode: SchedulingMode,
        selectedDays: DayOfWeek?,
        recurrenceInterval: Int,
        recurrenceUnit: RecurrenceUnit,
        startDate: LocalDate?, // AGGIUNGI QUESTO PARAMETRO
        hour: Int,
        minute: Int,
        drugName: String,
        dosage: String,
        dosageUnit: DosageUnit,
        drugNameExtra: String
    ) {
        Log.d("AppPreferences", "Chiamato saveProphylaxisConfig con startDate: $startDate")
        Log.d("AppPreferences", "Salvando configurazione nel DataStore...")
        context.dataStore.edit { preferences ->
            preferences[SCHEDULING_MODE] = schedulingMode.name
            preferences[SELECTED_DAYS] = selectedDays?.name ?: ""
            preferences[RECURRENCE_INTERVAL] = recurrenceInterval
            preferences[RECURRENCE_UNIT] = recurrenceUnit.name
            preferences[START_DATE] = startDate.toString() // AGGIUNGI QUESTA RIGA
            preferences[HOUR] = hour
            preferences[MINUTE] = minute
            preferences[DRUG_NAME] = drugName
            preferences[DOSAGE] = dosage
            preferences[DRUG_NAME_EXTRA] = drugNameExtra
            preferences[PROPHYLAXIS_DOSAGE_UNIT] = dosageUnit.name
            preferences[ACTIVE_PROPHYLAXIS] = true
        }
        Log.d("AppPreferences", "Configurazione salvata!")

    }

    data class ProphylaxisConfig(
        val schedulingMode: SchedulingMode,
        val selectedDays: DayOfWeek?,
        val recurrenceInterval: Int,
        val recurrenceUnit: RecurrenceUnit,
        val startDate: LocalDate?,
        val hour: Int,
        val minute: Int,
        val drugName: String,
        val dosage: String,
        val dosageUnit: String?,
        val drugNameExtra: String,
        val isActive: Boolean = true
    )

    val prophylaxisConfig: Flow<ProphylaxisConfig?> = context.dataStore.data
        .map { preferences ->
            try {
                val mode = SchedulingMode.valueOf(preferences[SCHEDULING_MODE] ?: return@map null)
                val days = preferences[SELECTED_DAYS]
                val interval = preferences[RECURRENCE_INTERVAL] ?: 1
                val unit = RecurrenceUnit.valueOf(preferences[RECURRENCE_UNIT] ?: "Days")
                val startDateString = preferences[START_DATE] // AGGIUNGI QUESTA RIGA
                val hour = preferences[HOUR] ?: 8
                val minute = preferences[MINUTE] ?: 0
                val drug = preferences[DRUG_NAME] ?: ""
                val dose = preferences[DOSAGE] ?: ""
                val dosageUnit = preferences[PROPHYLAXIS_DOSAGE_UNIT] ?: ""
                val drugNameExtra = preferences[DRUG_NAME_EXTRA] ?: ""

                // AGGIUNGI questo parsing per startDate:
                val startDate = try {
                    if (startDateString.isNullOrEmpty()) null
                    else LocalDate.parse(startDateString)
                } catch (e: Exception) {
                    null
                }

                ProphylaxisConfig(
                    schedulingMode = mode,
                    selectedDays = if (days.isNullOrEmpty()) null else DayOfWeek.valueOf(days),
                    recurrenceInterval = interval,
                    recurrenceUnit = unit,
                    startDate = startDate,
                    hour = hour,
                    minute = minute,
                    drugName = drug,
                    dosage = dose,
                    dosageUnit = preferences[PROPHYLAXIS_DOSAGE_UNIT]?.let {
                        try {
                            DosageUnit.valueOf(it).name
                        } catch (e: IllegalArgumentException) {
                            DosageUnit.MG_KG.name
                        }
                    } ?: DosageUnit.MG_KG.name,
                    drugNameExtra = drugNameExtra
                )
            } catch (e: Exception) {
                Log.e("AppPreferences", "Errore nel parsing della configurazione", e)
                null
            }
        }


    suspend fun clearProphylaxisConfig() {
        context.dataStore.edit { preferences ->
            preferences.remove(SCHEDULING_MODE)
            preferences.remove(SELECTED_DAYS)
            preferences.remove(RECURRENCE_INTERVAL)
            preferences.remove(RECURRENCE_UNIT)
            preferences.remove(START_DATE)
            preferences.remove(HOUR)
            preferences.remove(MINUTE)
            preferences.remove(DRUG_NAME)
            preferences.remove(DOSAGE)
            preferences.remove(PROPHYLAXIS_DOSAGE_UNIT)
            preferences.remove(DRUG_NAME_EXTRA)
            preferences.remove(ACTIVE_PROPHYLAXIS)
        }
    }

}