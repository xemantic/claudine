package com.xemantic.claudine.tool

import com.xemantic.anthropic.cache.CacheControl
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.text.Text
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.ToolInput
import com.xemantic.anthropic.tool.ToolResult
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlin.io.encoding.ExperimentalEncodingApi

@AnthropicTool("ReadFiles")
@Description(
  "Reads text files from human's machine. " +
      "All the files will be packaged into as single text content, " +
      "where each file is surrounded by the <file></file> tags." +
      "Each file tag will also have the path attribute, and possible error attribute, " +
      "containing error message, if reading a file threw an exception."
)
data class ReadFiles(
  @Description("The list of absolute file paths.")
  val paths: List<String>,
  @Description(
    "Indicates whether tool result of reading the files should be cached or not. " +
        "Defaults to false if omitted."
  )
  val cache: Boolean? = false
) : ToolInput {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String) = ToolResult(
    toolUseId = toolUseId,
    content = listOf(Text(text = paths.toContent())),
    cacheControl =
      if (cache == true) CacheControl(type = CacheControl.Type.EPHEMERAL)
      else null
  )

}

private fun List<String>.toContent() = joinToString(separator = "\n") { path ->
  try {
    val content = Path(path).readText()
    "<file path=\"$path\">\n$content\n</file>"
  } catch (e: Exception) {
    "<file path=\"$path\" error=\"${e.message}\"></file>"
  }
}

fun Path.readText() = SystemFileSystem.source(
  this
).buffered().use {
  it.readString()
}
