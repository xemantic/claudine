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

package com.xemantic.ai.claudine.tool

import com.xemantic.ai.claudine.getShellCommand
import java.io.File
import java.util.concurrent.TimeUnit

actual fun ExecuteShellCommand.execute(): ExecutionResult = ProcessBuilder(
    getShellCommand() + command
)
    .directory(File(workingDir.sanitizePath()))
    .redirectErrorStream(true)
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .start().let {
        val exited = it.waitFor(timeout.toLong(), TimeUnit.SECONDS)
        val output = it.inputStream.bufferedReader().readText()
        if (exited) ExecutionResult(exitCode = it.exitValue(), output)
        else ExecutionResult(timeout = true, output = output)
    }
