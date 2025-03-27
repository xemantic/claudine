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

import com.xemantic.ai.file.magic.readText
import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.io.files.Path
import kotlinx.serialization.SerialName

@SerialName("ReadFiles")
@Description("""Reads text files from human's machine.

- All the files will be packaged into as single text content, where each file is surrounded by the <file></file> tags.
- Each <file> tag will also have the path attribute.
- In case of an error reading file a possible error attribute will pop up containing error message.
""")
data class ReadFiles(
    @Description("The list of absolute file paths.")
    val paths: List<String>,
    @Description("The purpose of reading these files")
    override val purpose: String
) : ClaudineTool {

    fun use() = paths.joinToString(
        separator = "\n"
    ) { path ->
        try {
            val content = Path(path).readText()
            "<file path=\"$path\">\n$content\n</file>"
        } catch (e: Exception) {
            "<file path=\"$path\" error=\"${e.message}\"></file>"
        }
    }

    override val info get() = paths.pathInfo()

}
