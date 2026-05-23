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

import com.xemantic.ai.anthropic.content.Document
import com.xemantic.ai.anthropic.content.Image
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.content.ToolUse
import com.xemantic.ai.anthropic.message.MessageResponse
import com.xemantic.ai.file.magic.detectMediaType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface ClaudineTool {
    val purpose: String
    val info: String
}

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toContent() = when (detectMediaType()) {
    in Image.SUPPORTED_MEDIA_TYPES -> Image(this)
    in Document.SUPPORTED_MEDIA_TYPES -> Document(this)
    else -> Text(text = Base64.encode(this)) // non-recognized binary format
}

fun List<String>.pathInfo() = joinToString(
    separator = "\n",
    transform = { "- $it" }
)

fun String.formatAsToolDescription() = "|\n${toFormattedLines()}\n|"

private fun String.toFormattedLines(): String = lines().joinToString(
    separator = "\n"
) { "| $it" }

fun MessageResponse.describeTools() = content.filterIsInstance<ToolUse>().forEach {
    val input = it.resolveInput()
    println("[Claudine]> Using ${it.name.removePrefix("toolu_functions.")} to: ${input.purpose}")
    println(input.info.formatAsToolDescription())
}

private fun ToolUse.resolveInput() = when (name.removePrefix("toolu_functions.")) {
    "CreateFile" -> decodeInput<CreateFile>()
    "ExecuteShellCommand" -> decodeInput<ExecuteShellCommand>()
    "OpenUrl" -> decodeInput<OpenUrl>()
    "ReadBinaryFiles" -> decodeInput<ReadBinaryFiles>()
    "ReadFiles" -> decodeInput<ReadFiles>()
    else -> throw IllegalStateException("Unknown tool: $name")
}
