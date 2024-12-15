package com.xemantic.claudine

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Might be needed for other models
fun currentTimeSystemPrompt(): String {
  val now = Clock.System.now()
  val timeZone = TimeZone.currentSystemDefault()
  val dateTime = now.toLocalDateTime(timeZone)
  val dayOfWeek = dateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
  val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
  return "The current date is $dayOfWeek, $month ${dateTime.dayOfMonth}, ${dateTime.year} " +
      "at ${dateTime.asOnClock} ${timeZone.id}"
}

private val LocalDateTime.asOnClock get() =
  "${this.hour.time}:${this.minute.time}:${this.second.time}"

private val Int.time get() = toString().padStart(2, '0')
