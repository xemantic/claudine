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

import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.buffered
import kotlinx.io.writeString
import kotlinx.io.readString
import kotlinx.serialization.Serializable

@SerialName("Memory")
@Description(
    "Stores and retrieves information for Claudine across sessions. " +
    "This tool can save string values, retrieve them, list stored keys, and delete entries from memory."
)
data class Memory(
    @Description("The purpose of this memory operation")
    override val purpose: String,
    @Description("The operation to perform: 'store', 'retrieve', 'list', or 'delete'")
    val operation: String,
    @Description("The key to use for storing or retrieving a memory item")
    val key: String? = null,
    @Description("The value to store (only used with 'store' operation)")
    val value: String? = null
) : ClaudineTool {

    companion object {
        private const val MEMORY_FILE = ".claudine_memory.json"
        private val json = Json { prettyPrint = true }
    }

    @Serializable
    private data class MemoryStorage(
        val items: MutableMap<String, String> = mutableMapOf()
    )

    private fun getMemoryFilePath(): Path {
        val homeDir = System.getProperty("user.home")
        return Path("$homeDir/$MEMORY_FILE")
    }

    private fun loadMemory(): MemoryStorage {
        val memoryPath = getMemoryFilePath()
        return if (SystemFileSystem.exists(memoryPath)) {
            try {
                val content = SystemFileSystem.source(memoryPath).buffered().use { it.readString() }
                json.decodeFromString<MemoryStorage>(content)
            } catch (e: Exception) {
                // If there's an error reading or parsing the file, return an empty storage
                MemoryStorage()
            }
        } else {
            MemoryStorage()
        }
    }

    private fun saveMemory(memory: MemoryStorage) {
        val memoryPath = getMemoryFilePath()
        SystemFileSystem.sink(memoryPath).buffered().use { sink ->
            sink.writeString(json.encodeToString(memory))
        }
    }

    fun use(): String {
        return when (operation.lowercase()) {
            "store" -> storeMemory()
            "retrieve" -> retrieveMemory()
            "list" -> listMemories()
            "delete" -> deleteMemory()
            else -> "Unknown operation: $operation. Supported operations are: 'store', 'retrieve', 'list', and 'delete'."
        }
    }

    private fun storeMemory(): String {
        if (key.isNullOrBlank()) {
            return "Error: Key cannot be empty for the 'store' operation."
        }
        if (value == null) {
            return "Error: Value cannot be null for the 'store' operation."
        }

        val memory = loadMemory()
        memory.items[key] = value
        saveMemory(memory)

        return "Successfully stored memory with key: $key"
    }

    private fun retrieveMemory(): String {
        if (key.isNullOrBlank()) {
            return "Error: Key cannot be empty for the 'retrieve' operation."
        }

        val memory = loadMemory()
        return memory.items[key] ?: "No memory found for key: $key"
    }

    private fun listMemories(): String {
        val memory = loadMemory()
        return if (memory.items.isEmpty()) {
            "No memories stored."
        } else {
            "Stored memory keys:\n" + memory.items.keys.joinToString("\n") { "- $it" }
        }
    }

    private fun deleteMemory(): String {
        if (key.isNullOrBlank()) {
            return "Error: Key cannot be empty for the 'delete' operation."
        }

        val memory = loadMemory()
        return if (memory.items.remove(key) != null) {
            saveMemory(memory)
            "Successfully deleted memory with key: $key"
        } else {
            "No memory found for key: $key"
        }
    }

    override val info: String get() = "Operation: $operation${if (key != null) ", Key: $key" else ""}"
}