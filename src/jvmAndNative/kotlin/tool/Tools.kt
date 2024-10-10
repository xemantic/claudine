package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.tool.UsableTool

interface SafeTool : UsableTool {

  fun execute(toolUseId: String): ToolResult

  override fun use(toolUseId: String): ToolResult = try {
    println("[Tool][$this]")
    execute(toolUseId)
  } catch (e: Exception) {
    e.printStackTrace()
    ToolResult(
      toolUseId = toolUseId,
      isError = true,
      content = listOf(
        Text( // TODO it seems that it is a different class than input, because cache control is forbidden here
          text = e.message ?: "Unknown error occurred"
        )
      )
    )
  }

}
