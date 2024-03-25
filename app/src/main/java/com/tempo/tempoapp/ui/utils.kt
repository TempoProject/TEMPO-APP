package com.tempo.tempoapp.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Long.toStringDate(): String =
    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(this))

fun Long.toStringTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
