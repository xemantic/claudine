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

import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.serialization.SerialName

interface WithPurpose {
    val purpose: String
}

@SerialName("ExecuteShellCommand")
@Description("""Executes a shell command on human's machine with specified parameters.

Shell Selection:
- Unix systems (Linux, Mac): Uses bash
- Windows: Uses PowerShell

Behavior:
- If the command times out, the process is terminated and an error is returned
- Exit codes and standard output/error are captured
- Working directory must exist and be accessible
"""")
data class ExecuteShellCommand(
    @Description("The shell command to execute")
    val command: String,
    @Description("The purpose of running this command")
    override val purpose: String,
    @Description(
        "The directory where the command will be executed (absolute path recommended), " +
                "if omitted or null, the current working dir will be used"
    )
    val workingDir: String? = null,
    @Description("The timeout is defined in seconds")
    val timeout: Int
) : WithPurpose

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
) : WithPurpose

@SerialName("ReadBinaryFiles")
@Description("""Reads binary files from human's machine, so they can be analyzed.

- Image formats supported by Claude will be provided according to their respective content types.
- The contents of other types of files will transferred as Base64 encoded text content.
""")
data class ReadBinaryFiles(
    @Description(
        "The list of absolute file paths. " +
                "The order of file content in tool result will much the order of file paths."
    )
    val paths: List<String>,
    @Description("The purpose of reading these files")
    override val purpose: String
) : WithPurpose

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
) : WithPurpose

@SerialName("OpenUrl")
@Description("Reads contents of an URL")
data class OpenUrl(
    val url: String,
    @Description("The purpose of opening this URL")
    override val purpose: String
) : WithPurpose
