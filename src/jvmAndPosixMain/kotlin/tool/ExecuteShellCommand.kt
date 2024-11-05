package com.xemantic.claudine.tool

import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.ToolInput

@AnthropicTool("ExecuteShellCommand")
@Description("Executes given shell command on human's machine")
data class ExecuteShellCommand(
  val command: String,
  val workingDir: String,
  @Description("The timeout is defined in seconds")
  val timeout: Int
) : ToolInput() {

  init {
    use {
      executeShell(
        command, workingDir, timeout
      )
    }
  }

}

expect fun executeShell(
  command: String,
  workingDir: String,
  timeout: Int
): String
