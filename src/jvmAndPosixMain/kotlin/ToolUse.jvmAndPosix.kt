package com.xemantic.ai.claudine

import com.xemantic.ai.anthropic.content.Content
import com.xemantic.ai.anthropic.content.Document
import com.xemantic.ai.anthropic.content.Image
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.file.magic.detectMediaType
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
actual fun ReadBinaryFiles.use(): List<Content> = paths.map { path ->
    val bytes = Path(path).toBytes()  // TODO this should go to lib
    val mediaType = bytes.detectMediaType()
    when (mediaType) {
        in Image.SUPPORTED_MEDIA_TYPES -> Image(path)
        in Document.SUPPORTED_MEDIA_TYPES -> Document(path)
        else -> Text(text = Base64.encode(bytes)) // non-recognized binary format
    }
}

fun Path.toBytes(): ByteArray = SystemFileSystem.source(
    this
).buffered().use {
    it.readByteArray()
}
