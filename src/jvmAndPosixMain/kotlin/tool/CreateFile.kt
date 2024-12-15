package com.xemantic.claudine.tool

import com.xemantic.ai.anthropic.tool.AnthropicTool
import com.xemantic.ai.anthropic.tool.ToolInput
import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@AnthropicTool("CreateFile")
@Description("Creates a file on human's machine")
data class CreateFile(
  @Description("The absolute file path")
  val path: String,
  @Description("The content to write")
  val fileContent: String,
  @Description(
    "If the file content is binary, it will be transferred as Base64 encoded string." +
        "Defaults to false if omitted."
  )
  val base64: Boolean? = null
) : ToolInput() {

  init {
    use {
      val file = Path(path = path)
      SystemFileSystem.sink(file).buffered().use { sink ->
        if (base64 == true) {
          sink.write(Base64.decode(fileContent))
        } else {
          sink.writeString(fileContent)
        }
      }
      "File created"
    }
  }

}
