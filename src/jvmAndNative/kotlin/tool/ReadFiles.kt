package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.tool.SerializableTool
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@SerializableTool(
  name = "ReadFiles",
  description = """Reads files from specified paths on human's machine.
The order of text or image content elements in the returned tool result will match the order of requested files."""
)
data class ReadFiles(
  val paths: List<String>,
  val useBase64: Boolean
) : SafeTool {

  @OptIn(ExperimentalEncodingApi::class)
  override fun execute(toolUseId: String): ToolResult {
    val content = paths.map { path ->
      val file = Path(path)
      println("[Tool:ReadTextFiles] $file")
      SystemFileSystem.source(file).buffered().use { source ->
        val data = source.readByteArray()
        if (data.isImage() || useBase64) {
          Base64.encode(data)
        } else {
          data.decodeToString()
        }
      }
    }.map {
      Text(text = it)
    }
    return ToolResult(
      toolUseId = toolUseId,
      content = content
    )
  }

}

private fun ByteArray.isImage() = ImageFormatMagic.isImage(this)

fun ByteArray.startsWith(
  prefix: ByteArray
): Boolean =
  (size >= prefix.size)
      && slice(0 until prefix.size)
        .toByteArray()
        .contentEquals(prefix)

@OptIn(ExperimentalUnsignedTypes::class)
enum class ImageFormatMagic(
  vararg magic: UByte,
  private val test: (
    data: ByteArray,
    magic: ByteArray
  ) -> Boolean = { data, magic ->
    data.startsWith(magic)
  }
) {

  JPEG(0xFFu, 0xD8u, 0xFFu),
  PNG(0x89u, 0x50u, 0x4Eu, 0x47u, 0x0Du, 0x0Au, 0x1Au, 0x0Au),
  GIF(*"GIF8".toUByteArray()),
  WEBP(*"WEBP".toUByteArray(), test = { magic, data ->
    (data.size >= 12) && data.slice(8..11).toByteArray().contentEquals(magic)
  });

  private val magic = magic.toUByteArray()

  companion object {
    fun isImage(data: ByteArray) = (data.size >= 12) && entries.any { it.test(data, it.magic.toByteArray()) }
  }

}

@OptIn(ExperimentalUnsignedTypes::class)
private fun String.toUByteArray() = toCharArray().map { it.code.toUByte() }.toUByteArray()
