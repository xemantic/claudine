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
