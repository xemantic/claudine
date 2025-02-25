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
import kotlinx.cinterop.value
import platform.posix.*
import kotlin.concurrent.AtomicInt
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Duration.Companion.seconds

actual fun ExecuteShellCommand.use(): String {
    val workingDirectory = workingDir.sanitizePath()
    
    // Create temporary files for capturing stdout and stderr
    val tempStdoutPath = createTempFile("claudine", "stdout")
    val tempStderrPath = createTempFile("claudine", "stderr")
    
    // Build the command with redirection
    val fullCommand = "${getShellCommand()[0]} ${getShellCommand()[1]} \"cd $workingDirectory && $command > $tempStdoutPath 2> $tempStderrPath\""
    
    // Execute the command
    // TODO finish this implementation
    //val exitCode = executeWithTimeout(fullCommand, timeout.seconds)
    val exitCode = -1

    // Read the output files
//    val stdout = readFileContent(tempStdoutPath)
//    val stderr = readFileContent(tempStderrPath)
//
//    // Clean up temporary files
//    try {
//        unlink(tempStdoutPath)
//        unlink(tempStderrPath)
//    } catch (e: Exception) {
//        // Ignore cleanup errors
//    }
    
//    return if (exitCode != 0) {
//        "Command exited with non-zero status: $exitCode\nStdout:\n$stdout\nStderr:\n$stderr"
//    } else {
//        stdout
//    }
    TODO("Not implemented yet")
}

//@OptIn(ExperimentalForeignApi::class)
//private fun executeWithTimeout(command: String, timeout: kotlin.time.Duration): Int {
//    val exitCode = AtomicInt(0)
//    val isRunning = AtomicInt(1)
//
//    // Start process
//    val pid = fork()
//
//    when {
//        pid < 0 -> {
//            // Fork failed
//            return -1
//        }
//        pid == 0 -> {
//            // Child process
//            system(command)
//            exit(0)
//        }
//        else -> {
//            // Parent process
//            // Set up a timer to kill the process if it exceeds the timeout
//            val startTime = time(null)
//            val timeoutSeconds = timeout.inWholeSeconds
//
//            while (isRunning.value > 0) {
//                // Check if process has finished
//                val status = kotlinx.cinterop.alloc<IntVar>()
//                val result = waitpid(pid, status.ptr, WNOHANG)
//
//                if (result == pid) {
//                    // Process finished
//                    exitCode.value = status.value shr 8 and 0xFF  // Extract exit code
//                    isRunning.value = 0
//                } else {
//                    // Check if we've exceeded the timeout
//                    val currentTime = time(null)
//                    if (currentTime - startTime > timeoutSeconds) {
//                        // Kill the process
//                        kill(pid, SIGKILL)
//                        waitpid(pid, null, 0)  // Clean up zombie
//                        return -1  // Indicate timeout
//                    }
//
//                    // Sleep briefly to avoid busy-waiting
//                    usleep(100_000u)  // 100ms
//                }
//            }
//
//            return exitCode.value
//        }
//    }
//}

@OptIn(ExperimentalForeignApi::class)
private fun createTempFile(prefix: String, suffix: String): String {
    val tempDir = "/tmp"  // Standard temp directory on Unix-like systems
    val uniqueId = time(null).toString() + "_" + getpid().toString()
    return "$tempDir/${prefix}_${uniqueId}_$suffix"
}

//private fun readFileContent(path: String): String {
//    return try {
//        Path(path).let { filePath ->
//            if (SystemFileSystem.exists(filePath)) {
//                SystemFileSystem.source(filePath).use { source ->
//                    source.rea
//                }
//            } else {
//                ""
//            }
//        }
//    } catch (e: Exception) {
//        "Error reading file: ${e.message}"
//    }
//}

@OptIn(ExperimentalForeignApi::class)
private fun String?.sanitizePath(): String {
    if (this == null) {
        return "."
    }
    
    // Handle home directory expansion
    val homeDir = getenv("HOME")?.toKString() ?: "."
    return if (this.startsWith("~")) {
        this.replaceFirst("~", homeDir)
    } else {
        this
    }
}