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
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.SerialName
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.use

@SerialName("CreateFile")
@Description("Creates a file on human's machine")
data class CreateFile(
    @Description("The absolute file path")
    val path: String,
    @Description("The purpose of creating this file")
    override val purpose: String,
    @Description("The content to write")
    val content: String,
    @Description(
        "Optional content encoding flag. If the file content is binary, it will be transferred as Base64 encoded string. " +
                "If omitted then defaults to false."
    )
    val base64: Boolean? = null
) : ClaudineTool {

    fun use() {
        val file = Path(path = path)
        if (file.parent != null) { // if parent is null, we are in the current dir
            SystemFileSystem.createDirectories(file.parent!!)
        }
        SystemFileSystem.sink(file).buffered().use { sink ->
            if (base64 == true) {
                sink.write(
                    @OptIn(ExperimentalEncodingApi::class)
                    Base64.decode(content)
                )
            } else {
                sink.writeString(content)
            }
        }
    }

    override val info: String get() = path

}
