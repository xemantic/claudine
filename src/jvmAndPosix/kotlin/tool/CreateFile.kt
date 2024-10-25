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

@AnthropicTool("createFile")
@Description("Creates a file on human's machine")
data class CreateFile(
  @Description("The absolute path of the file")
  val path: String,
  @Description("The content to write")
  val content: String,
  @Description(
    "If the file content is binary, it will be transferred as Base64 encoded string." +
          "Defaults to false if omitted."
  )
  val base64: Boolean? = false,
) : UsableTool {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val file = Path(path = path)
    SystemFileSystem.sink(file).buffered().use { sink ->
      if (base64 == true) {
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
