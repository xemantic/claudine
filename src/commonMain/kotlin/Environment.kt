/*
 * claudine - an autonomous and Unix-omnipotent AI agent using Anthropic API
 * Copyright (C) 2025 Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.claudine

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect val operatingSystem: String

expect val userHomeDir: String

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
    listOf("/bin/bash", "-c")
}

// time details to be moved together
private val LocalDateTime.asOnClock
    get() =
        "${this.hour.time}:${this.minute.time}:${this.second.time}"

private val Int.time get() = toString().padStart(2, '0')
