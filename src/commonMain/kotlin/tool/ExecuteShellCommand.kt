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

import com.xemantic.ai.claudine.userHomeDir
import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.serialization.SerialName

@SerialName("ExecuteShellCommand")
@Description("""Executes a shell command on human's machine with specified parameters.

Shell Selection:
- Unix systems (Linux, Mac): Uses bash
- Windows: Uses PowerShell

Behavior:
- If the command times out, the process is terminated and an error is returned
- Exit codes and standard output/error are captured
- Working directory must exist and be accessible
"""")
data class ExecuteShellCommand(
    @Description("The purpose of running this command")
    override val purpose: String,
    @Description("The shell command to execute")
    val command: String,
    @Description(
        "The directory where the command will be executed (absolute path recommended), " +
                "if omitted or null, the current working dir will be used"
    )
    val workingDir: String? = null,
    @Description("The timeout is defined in seconds")
    val timeout: Int
) : ClaudineTool {

    fun use(): String {
        val result = execute()
        return if (result.timeout) {
            "Command timed out:\n\n${result.output}"
        } else if (result.exitCode != 0) {
            "Command exited with non-zero code: ${result.exitCode}\n\n${result.output}"
        } else {
            result.output
        }
    }

    override val info get() = $$"""
        $ $$command
        
        working dir: $${workingDir ?: "."}
        timeout: $$timeout seconds
    """.trimIndent()

}

data class ExecutionResult(
    val exitCode: Int = -1,
    val output: String,
    val timeout: Boolean = false
)

expect fun ExecuteShellCommand.execute(): ExecutionResult

internal fun String?.sanitizePath(): String = when {
    this == null -> "."
    startsWith("~") -> replace("~", userHomeDir)
    else -> this
}
