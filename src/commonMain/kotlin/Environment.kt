package com.xemantic.ai.claudine

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect val operatingSystem: String

// Might be needed for other agents, so we can move it to a library
fun describeCurrentMoment(): String {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val dateTime = now.toLocalDateTime(timeZone)
    val dayOfWeek = dateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "The current date is $dayOfWeek, $month ${dateTime.dayOfMonth}, ${dateTime.year} " +
            "at ${dateTime.asOnClock} ${timeZone.id}"
}

fun getShellCommand() = if (
    operatingSystem.lowercase().contains("win")
) {
    listOf("powershell.exe", "-Command")
} else {
    listOf("sh", "-c")
}

// time details to be moved together
private val LocalDateTime.asOnClock get() =
    "${this.hour.time}:${this.minute.time}:${this.second.time}"

private val Int.time get() = toString().padStart(2, '0')
