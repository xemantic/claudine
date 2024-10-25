package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool

@AnthropicTool("executeShellCommand")
@Description("Executes given shell command on human's machine")
data class ExecuteShellCommand(
  val command: String,
  val workingDir: String,
  @Description("The timeout is defined in seconds")
  val timeout: Int
) : UsableTool {

  override suspend fun use(toolUseId: String) = executeShell(
    toolUseId, command, workingDir, timeout
  )

}

expect fun executeShell(
  toolUseId: String,
  command: String,
  workingDir: String,
  timeout: Int
): ToolResult
