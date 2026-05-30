package com.example.nfcmuseumguide.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatMuseumTime(): String =
    SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(this))
