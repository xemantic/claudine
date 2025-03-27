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
import com.xemantic.ai.file.magic.detectMediaType
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface ClaudineTool {
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
