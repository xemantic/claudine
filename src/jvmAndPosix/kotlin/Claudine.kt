package com.xemantic.claudine

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.message.Message
import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.message.ToolUse
import com.xemantic.claudine.tool.CreateFile
import com.xemantic.claudine.tool.ExecuteShellCommand
import com.xemantic.claudine.tool.ReadFiles
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

  val client = Anthropic {
    anthropicBeta = "prompt-caching-2024-07-31"
    tool<ExecuteShellCommand>()
    tool<ReadFiles>()
    tool<CreateFile>()
  }

  println("Connecting human and human's machine to Claude AI")

  var conversation = mutableListOf<Message>()

  while (true) {
    print("> ")
    val line = readln()
    if (line == "exit") { return@runBlocking }

    conversation += Message { +line } // no cache involved

    var continueRequest = true
    do {

      val response = client.messages.create {
        system(claudineSystemPrompt)
        messages = conversation
        maxTokens = 4096 * 2 // for the latest model
        useTools()
      }

      conversation += response.asMessage()

      val toolResults = mutableListOf<ToolResult>()
      response.content.forEach {
        when (it) {
          is Text -> {
            println("[Claude]: ${it.text}")
          }
          is ToolUse -> {
            println("[ToolUse]: $it")
            println("[ToolUse]: Do you allow Claude to execute this command? [yes/no]")
            print("> ")
            val confirmLine = readln()
            val result = if (confirmLine == "yes") {
              it.use()
            } else {
              ToolResult(
                toolUseId = it.id,
                "Human refused to run this command on their machine"
              )
            }
            toolResults += result
          }
          else -> println("Unexpected content type: $it")
        }
      }

      continueRequest = toolResults.isNotEmpty()
      if (continueRequest) {
        conversation += Message {
          content += toolResults
        }
      }

    } while (continueRequest)
  }
}
