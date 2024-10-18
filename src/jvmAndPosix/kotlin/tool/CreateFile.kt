package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// TODO it should be replaced with CreateFiles
@AnthropicTool("CreateFile")
@Description("""
## Creates a file

A file, named according to the path, containing the content, will be created on human's machine.

A binary data content can be created with a combination of Base64 encoded content and optional base64 parameter set to true.
The base64 parameter defaults to false.
"""
)
data class CreateFile(
  val path: String,
  val content: String,
  val base64: Boolean = false,
) : UsableTool {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val file = Path(path = path)
    SystemFileSystem.sink(file).buffered().use { sink ->
      if (base64) {
        sink.write(Base64.decode(content))
      } else {
        sink.writeString(content)
      }
    }
    return ToolResult(
      toolUseId = toolUseId,
      content = listOf(
        Text(text = "File created")
      )
    )
  }

}
