package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.CacheControl
import com.xemantic.anthropic.message.Image
import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@AnthropicTool("ReadFiles")
@Description("""
## Reads files

Reads specified files from human's machine.
 
The files are read according to the list of specified file descriptors. Each descriptor refers to the file path
and an optional base64 flag, which defaults to false. If base64 is set to true, the file will be encoded
with Base64.

The order of text or image content elements in the returned tool result will match the order of requested files.

Setting cache parameter to true will cause cache control to be added to the tool result. Caching is important for
big results containing several files, to minimize API usage costs.


""")
data class ReadFiles(
  val fileDescriptors: List<FileDescriptor>,
  val cache: Boolean
) : UsableTool {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val content = fileDescriptors.map { descriptor ->
      val file = Path(descriptor.path)
      println("[Tool:ReadTextFiles] $file")
      val data = SystemFileSystem.source(file).buffered().use {
        it.readByteArray()
      }
      val mediaType = data.hasImageMediaType()
      if (mediaType != null) {
        val content = Base64.encode(data)
        Image(Image.Source(mediaType = mediaType, data = content))
      } else {
        val content = data.decodeToString()
        Text(text = content)
      }
    }
    return ToolResult(
      toolUseId = toolUseId,
      content = content,
      cacheControl =
        if (cache) CacheControl(type = CacheControl.Type.EPHEMERAL)
        else null
    )
  }

}

@Serializable
@SerialName("FileDescriptor") //TODO what's default serial name
data class FileDescriptor(
  val path: String,
  val base64: Boolean = false
)

private fun ByteArray.hasImageMediaType() = ImageFormatMagic.isImage(this)?.mediaType

fun ByteArray.startsWith(
  prefix: ByteArray
): Boolean =
  (size >= prefix.size)
      && slice(0 until prefix.size)
        .toByteArray()
        .contentEquals(prefix)

@OptIn(ExperimentalUnsignedTypes::class)
enum class ImageFormatMagic(
  val mediaType: Image.MediaType,
  vararg magic: UByte,
  private val test: (
    data: ByteArray,
    magic: ByteArray
  ) -> Boolean = { data, magic ->
    data.startsWith(magic)
  }
) {

  JPEG(Image.MediaType.IMAGE_JPEG, 0xFFu, 0xD8u, 0xFFu),
  PNG(Image.MediaType.IMAGE_PNG, 0x89u, 0x50u, 0x4Eu, 0x47u, 0x0Du, 0x0Au, 0x1Au, 0x0Au),
  GIF(Image.MediaType.IMAGE_GIF, *"GIF8".toUByteArray()),
  WEBP(Image.MediaType.IMAGE_GIF, *"WEBP".toUByteArray(), test = { magic, data ->
    (data.size >= 12) && data.slice(8..11).toByteArray().contentEquals(magic)
  });

  private val magic = magic.toUByteArray()

  companion object {
    fun isImage(data: ByteArray): ImageFormatMagic? =
      if (data.size < 12) null
      else entries.find { it.test(data, it.magic.toByteArray()) }
  }

}

@OptIn(ExperimentalUnsignedTypes::class)
private fun String.toUByteArray() = toCharArray().map {
  it.code.toUByte()
}.toUByteArray()
