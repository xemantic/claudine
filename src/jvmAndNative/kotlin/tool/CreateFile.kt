package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.tool.SerializableTool
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@SerializableTool(
  name = "CreateFile",
  description = "Creates file, from content, under specified path, on human's machine"
)
data class CreateFile(
  val path: String,
  val content: String,
  val isBase64: Boolean = false,
) : SafeTool {

  @OptIn(ExperimentalEncodingApi::class)
  override fun execute(toolUseId: String): ToolResult {
    val file = Path(path = path)
    println("[Tool:CreateFile] $file")
    SystemFileSystem.sink(file).buffered().use { sink ->
      if (isBase64) {
        sink.write(Base64.decode(content))
      } else {
        sink.writeString(content)
      }
    }
    return ToolResult(
      toolUseId = toolUseId,
      content = listOf(
        Text(
          text = "File created",
        )
      )
    )
  }

}
