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

import com.xemantic.ai.anthropic.content.Content
import com.xemantic.ai.anthropic.content.Document
import com.xemantic.ai.anthropic.content.Image
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.file.magic.detectMediaType
import com.xemantic.ai.file.magic.readText
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.writeString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

expect fun ExecuteShellCommand.use(): String

@OptIn(ExperimentalEncodingApi::class)
fun CreateFile.use() {
    val file = Path(path = path)
    SystemFileSystem.createDirectories(file.parent!!)
    SystemFileSystem.sink(file).buffered().use { sink ->
        if (base64 == true) {
            sink.write(Base64.decode(content))
        } else {
            sink.writeString(content)
        }
    }
}

val CreateFile.info
    get() = """
|
| $path
|
| purpose: $purpose
|
""".trimIndent()

@OptIn(ExperimentalEncodingApi::class)
fun ReadBinaryFiles.use(): List<Content> = paths.map { path ->
    Path(path).toBytes().toContent()
}

fun Path.toBytes(): ByteArray = SystemFileSystem.source(
    this
).buffered().use {
    it.readByteArray()
}

val ExecuteShellCommand.info
    get() = $$"""
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

val ReadFiles.info
    get() = """
|    
${paths.pathInfo()}
|
| purpose: $purpose
|
""".trimIndent()

val ReadBinaryFiles.info
    get() = """
|
${paths.pathInfo()}
|
| purpose: $purpose
""".trimIndent()

suspend fun OpenUrl.use(client: HttpClient): Content {
    val response = client.get(url)
    val contentType = response.contentType()
    return if (contentType != null) {
        if (contentType.match(ContentType.Text.Html) || contentType.match(ContentType.Text.Plain)) {
            Text(response.bodyAsText())
        } else {
            response.body<ByteArray>().toContent()
        }
    } else {
        Text(response.bodyAsText())
    }
}

val OpenUrl.info
    get() = """
|
| url: $url
|
| purpose: $purpose
""".trimIndent()


fun getTooUseInfo(toolInput: Any) = when (toolInput) {
    is ExecuteShellCommand -> toolInput.info
    is CreateFile -> toolInput.info
    is ReadFiles -> toolInput.info
    is ReadBinaryFiles -> toolInput.info
    is OpenUrl -> toolInput.info
    else -> IllegalStateException("Unknown tool input: $toolInput")
}

private fun List<String>.pathInfo() = joinToString(
    separator = "\n",
    transform = { "| - $it" }
)

@OptIn(ExperimentalEncodingApi::class)
private fun ByteArray.toContent() = when (detectMediaType()) {
    in Image.SUPPORTED_MEDIA_TYPES -> Image(this)
    in Document.SUPPORTED_MEDIA_TYPES -> Document(this)
    else -> Text(text = Base64.encode(this)) // non-recognized binary format
}
