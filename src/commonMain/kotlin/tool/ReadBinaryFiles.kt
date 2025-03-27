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

import com.xemantic.ai.anthropic.content.Content
import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.SerialName
import kotlin.io.encoding.ExperimentalEncodingApi

@SerialName("ReadBinaryFiles")
@Description("""Reads binary files from human's machine, so they can be analyzed.

- Image formats supported by Claude will be provided according to their respective content types.
- The contents of other types of files will transferred as Base64 encoded text content.
""")
data class ReadBinaryFiles(
    @Description("The purpose of reading these files")
    override val purpose: String,
    @Description(
        "The list of absolute file paths. " +
                "The order of file content in tool result will much the order of file paths."
    )
    val paths: List<String>,
) : ClaudineTool {

    @OptIn(ExperimentalEncodingApi::class)
    fun use(): List<Content> = paths.map { path ->
        Path(path).toBytes().toContent()
    }

    override val info get() = paths.pathInfo()

}

fun Path.toBytes(): ByteArray = SystemFileSystem.source(
    this
).buffered().use {
    it.readByteArray()
}
