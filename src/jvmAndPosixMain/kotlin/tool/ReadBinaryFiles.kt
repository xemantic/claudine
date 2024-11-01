package com.xemantic.claudine.tool

import com.xemantic.anthropic.cache.CacheControl
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.text.Text
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.ToolInput
import com.xemantic.anthropic.tool.ToolResult
import kotlinx.io.files.Path
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@AnthropicTool("ReadBinaryFiles")
@Description("Reads binary files from human's machine. The contents of the files is Base64 encoded.")
data class ReadBinaryFiles(
  @Description(
    "The list of absolute file paths. " +
        "The order of file content in tool result will much the order of file paths."
  )
  val paths: List<String>,
  @Description(
    "Indicates whether tool result of reading the files should be cached or not. " +
        "Defaults to false if omitted."
  )
  val cache: Boolean? = false
) : ToolInput {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val content = paths.map { path ->
      val data = Path(path).toBytes()
      val content = Base64.encode(data)
      Text(text = content)
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
