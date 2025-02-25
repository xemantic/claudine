package com.xemantic.ai.claudine

import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.serialization.SerialName

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
    val purpose: String,
    @Description(
        "The directory where the command will be executed (absolute path recommended), " +
                "if omitted or null, the current working dir will be used"
    )
    val workingDir: String? = null,
    @Description("The timeout is defined in seconds")
    val timeout: Int
)

@SerialName("CreateFile")
@Description("Creates a file on human's machine")
data class CreateFile(
    @Description("The absolute file path")
    val path: String,
    @Description("The purpose of creating this file")
    val purpose: String,
    @Description("The content to write")
    val content: String,
    @Description(
        "Optional content encoding flag. If the file content is binary, it will be transferred as Base64 encoded string. " +
                "If omitted then defaults to false."
    )
    val base64: Boolean? = null
)

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
    val purpose: String
)

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
    val purpose: String
)
