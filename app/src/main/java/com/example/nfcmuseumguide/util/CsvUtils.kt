package com.example.nfcmuseumguide.util

fun String.csvEscape(): String = "\"${replace("\"", "\"\"")}\""
