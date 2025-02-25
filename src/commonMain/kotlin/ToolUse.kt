package com.xemantic.ai.claudine

import com.xemantic.ai.anthropic.content.Content
import com.xemantic.ai.file.magic.readText
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.use

expect fun ExecuteShellCommand.use(): String

@OptIn(ExperimentalEncodingApi::class)
fun CreateFile.use() {
    val file = Path(path = path)
    SystemFileSystem.createDirectories(file)
    SystemFileSystem.sink(file).buffered().use { sink ->
        if (base64 == true) {
            sink.write(Base64.decode(content))
        } else {
            sink.writeString(content)
        }
    }
}

val CreateFile.info get() = """
|
| $path
|
| purpose: $purpose
|
""".trimIndent()

expect fun ReadBinaryFiles.use(): List<Content>

val ExecuteShellCommand.info get() = $$"""
|
| $ $$command
|
| purpose: $$purpose
| working dir: $${workingDir ?: "."}
| timeout: $$timeout seconds
|
""".trimIndent()

fun ReadFiles.use() = paths.joinToString(
    separator = "\n"
) { path ->
    try {
        val content = Path(path).readText()
        "<file path=\"$path\">\n$content\n</file>"
    } catch (e: Exception) {
        "<file path=\"$path\" error=\"${e.message}\"></file>"
    }
}

val ReadFiles.info get() = """
|    
${paths.pathInfo()}
|
| purpose : $purpose
|
""".trimIndent()

val ReadBinaryFiles.info get() = """
|
${paths.pathInfo()}
|
| purpose : $purpose
""".trimIndent()

fun getTooUseInfo(toolInput: Any) = when (toolInput) {
    is ExecuteShellCommand -> toolInput.info
    is CreateFile -> toolInput.info
    is ReadFiles -> toolInput.info
    is ReadBinaryFiles -> toolInput.info
    else -> IllegalStateException("Unknown tool input: $toolInput")
}

private fun List<String>.pathInfo() = joinToString(
    separator = "\n",
    transform = { "| - $it" }
)
