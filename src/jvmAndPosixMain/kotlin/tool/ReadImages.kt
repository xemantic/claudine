package com.xemantic.claudine.tool

import com.xemantic.anthropic.cache.CacheControl
import com.xemantic.anthropic.image.Image
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.text.Text
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.ToolInput
import com.xemantic.anthropic.tool.ToolResult
import kotlinx.io.files.Path
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@AnthropicTool("ReadImages")
@Description("Reads images from human's machine")
data class ReadImages(
  @Description(
    "The list of absolute image file paths. " +
        "The order of file content in tool result will match the order of paths."
  )
  val paths: List<String>,
  @Description(
    "Indicates whether the image in tool result should be cached. Depending on the model, only limited " +
        "number of content elements can be cached, therefore this flag might be ignored."
  )
  val cache: Boolean? = false
) : ToolInput {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val content = paths.map { path ->
      val data = Path(path).toBytes()
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
