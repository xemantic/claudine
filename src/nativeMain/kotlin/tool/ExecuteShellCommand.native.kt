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
import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun ExecuteShellCommand.execute(): ExecutionResult {

    val sanitizedWorkingDir = workingDir.sanitizePath()

    // Create pipes for stdout/stderr
    val pipefd = IntArray(2)
    if (pipe(pipefd.refTo(0)) != 0) {
        return ExecutionResult(
            exitCode = -1,
            output = "Failed to create pipe: ${strerror(errno)?.toKString()}",
            timeout = false
        )
    }

    // Fork process
    val pid = fork()

    when (pid) {
        -1 -> {
            return ExecutionResult(
                exitCode = -1,
                output = "Fork failed: ${strerror(errno)?.toKString()}",
                timeout = false
            )
        }
        0 -> {
            // Child process
            try {
                // Change directory
                if (chdir(sanitizedWorkingDir) != 0) {
                    perror("chdir failed")
                    _exit(1)
                }

                // Close read end
                close(pipefd[0])

                // Redirect stdout and stderr to pipe
                dup2(pipefd[1], STDOUT_FILENO)
                dup2(pipefd[1], STDERR_FILENO)
                close(pipefd[1])

                // Build full command using the common getShellCommand function
                val shellArgs = getShellCommand()
                val fullCommand = shellArgs + command

                // Convert to C-style array
                memScoped {
                    val args = allocArray<CPointerVar<ByteVar>>(fullCommand.size + 1)
                    fullCommand.forEachIndexed { index, arg ->
                        args[index] = arg.cstr.ptr
                    }
                    args[fullCommand.size] = null

                    // Execute command
                    execvp(args[0]!!.toKString(), args)

                    // If we get here, exec failed
                    perror("execvp failed")
                }
            } finally {
                _exit(1)
            }
        }
        else -> {
            // Parent process
            close(pipefd[1]) // Close write end

            // Set up for timeout handling
            val startTime = time(null)
            val output = StringBuilder()
            var timedOut = false
            var exitCode = -1

            try {
                // Read output
                val buffer = ByteArray(4096)

                memScoped {
                    val status = alloc<IntVar>()
                    var processFinished = false

                    // Wait for process with timeout
                    while (!processFinished && (time(null) - startTime < timeout)) {
                        val waitResult = waitpid(pid, status.ptr, WNOHANG)

                        if (waitResult == pid) {
                            processFinished = true
                            exitCode = status.value
                        } else if (waitResult == -1) {
                            return ExecutionResult(
                                exitCode = -1,
                                output = "waitpid failed: ${strerror(errno)?.toKString()}",
                                timeout = false
                            )
                        }

                        // Read available output
                        buffer.usePinned { pinnedBuffer ->
                            val bytesRead = read(pipefd[0], pinnedBuffer.addressOf(0), buffer.size.toULong())
                            if (bytesRead > 0) {
                                val validString = buffer.sliceArray(0 until bytesRead.toInt()).decodeToString()
                                output.append(validString)
                            }
                        }

                        if (!processFinished) {
                            usleep(100_000u) // 100ms
                        }
                    }

                    // Kill if timeout
                    if (!processFinished) {
                        kill(pid, SIGKILL)
                        waitpid(pid, status.ptr, 0)
                        timedOut = true
                        output.append("\nCommand timed out after $timeout seconds and was terminated.")
                    }

                    // Read any remaining output with proper loop exit condition
                    var continueReading = true
                    while (continueReading) {
                        buffer.usePinned { pinnedBuffer ->
                            val bytesRead = read(pipefd[0], pinnedBuffer.addressOf(0), buffer.size.toULong())
                            if (bytesRead <= 0) {
                                continueReading = false
                            } else {
                                val validString = buffer.sliceArray(0 until bytesRead.toInt()).decodeToString()
                                output.append(validString)
                            }
                        }
                    }
                }
            } finally {
                close(pipefd[0])
            }
            
            return ExecutionResult(
                exitCode = exitCode,
                output = output.toString(),
                timeout = timedOut
            )
        }
    }
    
    // This should never be reached due to the returns in each branch above
    return ExecutionResult(
        exitCode = -1,
        output = "Unexpected execution path in native shell command execution",
        timeout = false
    )
}
