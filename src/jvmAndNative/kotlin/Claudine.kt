package com.xemantic.claudine

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.message.Message
import com.xemantic.anthropic.message.Role
import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.message.ToolUse
import com.xemantic.claudine.tool.CreateFile
import com.xemantic.claudine.tool.ExecuteShellCommand
import com.xemantic.claudine.tool.ReadFiles
import kotlinx.coroutines.runBlocking

val systemPrompt = """
    The human you are connected to is sending you messages through the Anthropic API. These API requests are
    invoked from the human's machine. You are provided with tools allowing to control human's machine.
  """.trimIndent()

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
        system(systemPrompt)
        messages = conversation
        maxTokens = 4096
        useTools()
      }

      conversation += Message {
        role = Role.ASSISTANT
        content += response.content
      }

      val toolResults = mutableListOf<ToolResult>()
      response.content.forEach {
        when (it) {
          is Text -> {
            println("[Claude]: ${it.text}")
          }
          is ToolUse -> {
            println("[ToolUse]: ${it.name}")
            val result =it.use()
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
