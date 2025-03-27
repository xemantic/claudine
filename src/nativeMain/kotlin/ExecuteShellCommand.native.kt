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

import kotlinx.cinterop.*
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun ExecuteShellCommand.use(): String {

    val sanitizedWorkingDir = workingDir.sanitizePath()

    // Create pipes for stdout/stderr
    val pipefd = IntArray(2)
    if (pipe(pipefd.refTo(0)) != 0) {
        throw Error("Failed to create pipe: ${strerror(errno)?.toKString()}")
    }

    // Fork process
    val pid = fork()

    when (pid) {
        -1 -> {
            throw Error("Fork failed: ${strerror(errno)?.toKString()}")
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

                // Build full command
                val fullCommand = listOf("bash", "-c") + command

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
                        } else if (waitResult == -1) {
                            throw Error("waitpid failed: ${strerror(errno)?.toKString()}")
                        }

                        // Read available output
                        buffer.usePinned { pinnedBuffer ->
                            val bytesRead = read(pipefd[0], pinnedBuffer.addressOf(0), buffer.size.toULong())
                            if (bytesRead > 0) {
                                output.append(buffer.toKString().substring(0, bytesRead.toInt()))
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
                    }

                    // Read any remaining output
                    while (true) {
                        buffer.usePinned { pinnedBuffer ->
                            val bytesRead = read(pipefd[0], pinnedBuffer.addressOf(0), buffer.size.toULong())
                            if (bytesRead <= 0) return@usePinned
                            output.append(buffer.toKString().substring(0, bytesRead.toInt()))
                        }
                    }
                }
            } finally {
                close(pipefd[0])
            }

            return output.toString()
        }
    }
    return ""
}

@OptIn(ExperimentalForeignApi::class)
private val userHomeDir = getenv("HOME")?.toKString()
    ?: getpwuid(getuid())?.pointed?.pw_dir?.toKString()
    ?: throw Error("Could not determine user home directory")

private fun String?.sanitizePath(): String =
    when {
        this == null -> "."
        startsWith("~") -> replace("~", userHomeDir)
        else -> this
    }
