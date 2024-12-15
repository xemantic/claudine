package com.xemantic.claudine.tool

import com.xemantic.ai.anthropic.cache.CacheControl
import com.xemantic.ai.anthropic.content.Image
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.content.isImage
import com.xemantic.ai.anthropic.tool.AnthropicTool
import com.xemantic.ai.anthropic.tool.ToolInput
import com.xemantic.ai.tool.schema.meta.Description
import com.xemantic.claudine.files.toBytes
import kotlinx.io.files.Path
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@AnthropicTool("ReadBinaryFiles")
@Description(
  "Reads binary files from human's machine, so they can be analyzed by the LLM. " +
      "Image formats supported by Claude will be provided according to their respective content types. " +
      "The contents of other types of files will transferred as Base64 encoded text content.")
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
) : ToolInput() {

  init {
    use {
      paths.forEach { path ->
        val bytes = Path(path).toBytes()
        when {
          bytes.isImage -> {
            +Image(path)
          }
          // The PDF in tool result is still not supported
//          bytes.isDocument -> {
//            +Document(path)
//          }
          else -> {
            +Text(text = Base64.encode(bytes))
          }
        }
      }
      // TODO should cache control be on result level?
      cacheControl =
        if (cache == true) CacheControl(type = CacheControl.Type.EPHEMERAL)
        else null
    }
  }

}
