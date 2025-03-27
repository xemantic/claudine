package com.xemantic.ai.claudine.tool

import kotlinx.cinterop.*
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
fun main() {
    memScoped {
        val commandLine = "cmd.exe /c dir"  // Command to execute

        // Convert commandLine to a mutable LPWSTR buffer
        val bufferLength = commandLine.length + 1
        val commandLineBuffer = allocArray<UShortVar>(bufferLength).apply {
            commandLine.forEachIndexed { index, char ->
                this[index] = char.code.toUShort()
            }
            this[commandLine.length] = 0u  // Null terminator
        }

        val startupInfo = alloc<STARTUPINFOW>().apply {
            cb = sizeOf<STARTUPINFOW>().toUInt()
        }

        val processInfo = alloc<PROCESS_INFORMATION>()

        val success = CreateProcessW(
            null,              // lpApplicationName (use command line)
            commandLineBuffer, // lpCommandLine (mutable buffer)
            null,              // lpProcessAttributes
            null,              // lpThreadAttributes
            0,                 // bInheritHandles
            0u,                // dwCreationFlags
            null,              // lpEnvironment (inherit parent)
            null,              // lpCurrentDirectory (inherit parent)
            startupInfo.ptr,   // lpStartupInfo
            processInfo.ptr    // lpProcessInformation
        )

        if (success == 0) {
            val errorCode = GetLastError()
            println("CreateProcess failed with error: $errorCode")
            return
        }

        // Wait for the process to finish
        WaitForSingleObject(processInfo.hProcess, INFINITE)

        // Retrieve exit code
        val exitCode = alloc<DWORDVar>().apply {
            GetExitCodeProcess(processInfo.hProcess, ptr)
        }.value

        println("Process exited with code: $exitCode")

        // Clean up handles
        CloseHandle(processInfo.hProcess)
        CloseHandle(processInfo.hThread)
    }
}
