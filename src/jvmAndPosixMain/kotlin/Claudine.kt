package com.xemantic.claudine

import com.xemantic.anthropic.Anthropic
import com.xemantic.claudine.tool.CreateFile
import com.xemantic.claudine.tool.ExecuteShellCommand
import com.xemantic.claudine.tool.ReadBinaryFiles
import com.xemantic.claudine.tool.ReadFiles
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {

  val autoConfirmToolUse = args.isNotEmpty() && args[0] == "-y"

  val anthropic = Anthropic {
    anthropicBeta = "prompt-caching-2024-07-31"
    tool<ExecuteShellCommand>()
    tool<ReadFiles>()
    tool<CreateFile>()
    tool<ReadBinaryFiles>()
  }

  runBlocking {
    claudine(anthropic, claudineSystemPrompt, autoConfirmToolUse)
  }

}
