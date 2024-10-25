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
@Description("Reads files from human's machine")
data class ReadFiles(
  @Description(
    "The list of file descriptors. " +
        "The order of file content in tool result will much the order of file descriptors."
  )
  val fileDescriptors: List<FileDescriptor>,
  @Description(
    "Indicates whether tool result of reading the files should be cached or not. " +
        "Defaults to false if omitted."
  )
  val cache: Boolean? = false
) : UsableTool {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val content = fileDescriptors.map { descriptor ->
      val data = Path(descriptor.path).toBytes()
      val mediaType = data.maybeImageMediaType()
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
        if (cache == true) CacheControl(type = CacheControl.Type.EPHEMERAL)
        else null
    )
  }

}

fun Path.toBytes(): ByteArray = SystemFileSystem.source(this).buffered().use {
  it.readByteArray()
}

@Serializable
@SerialName("fileDescriptor")
@Description("Describes the file to read")
data class FileDescriptor(
  val path: String,
  @Description(
    "If true, the file read result will be encoded as Base64 string, which is useful" +
        "for binary files. " +
        "Defaults to false if omitted. " +
        "Image files of supported formats will be encoded regardless of the value of this flag. "
  )
  val base64: Boolean? = false
)

// visible for testing
internal fun ByteArray.maybeImageMediaType() = ImageFormatMagic.findMagic(this)?.mediaType

fun ByteArray.startsWith(
  prefix: ByteArray
): Boolean =
  (size >= prefix.size)
      && slice(prefix.indices)
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
  WEBP(Image.MediaType.IMAGE_WEBP, *"WEBP".toUByteArray(), test = { data, magic ->
    (data.size >= 12) && data.slice(8..11).toByteArray().contentEquals(magic)
  });

  private val magic = magic.toUByteArray()

  companion object {
    fun findMagic(data: ByteArray): ImageFormatMagic? =
      if (data.size < 12) null
      else entries.find { it.test(data, it.magic.toByteArray()) }
  }

}

@OptIn(ExperimentalUnsignedTypes::class)
private fun String.toUByteArray() = toCharArray().map {
  it.code.toUByte()
}.toUByteArray()
