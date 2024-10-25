package com.xemantic.claudine

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.message.Message
import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.message.ToolUse
import com.xemantic.anthropic.message.plusAssign
import com.xemantic.claudine.tool.CreateFile
import com.xemantic.claudine.tool.ExecuteShellCommand
import com.xemantic.claudine.tool.ReadFiles
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {

  val autoConfirmToolUse = args.isNotEmpty() && args[0] == "-y"

  val client = Anthropic {
    anthropicBeta = "prompt-caching-2024-07-31"
    tool<ExecuteShellCommand>()
    tool<ReadFiles>()
    tool<CreateFile>()
  }

  println("Connecting human and human's machine to Claude AI")

  val conversation = mutableListOf<Message>()

  while (true) {
    print("> ")
    val line = readln()
    if (line == "exit") { return@runBlocking }

    conversation += Message { +line } // no cache involved

    var continueRequest: Boolean
    do {
      print("[Claude] ...Reasoning...")
      val response = client.messages.create {
        system(claudineSystemPrompt)
        messages = conversation
        maxTokens = 4096 * 2 // for the latest model
        useTools()
      }
      println()

      conversation += response

      val toolResults = mutableListOf<ToolResult>()
      response.content.forEach {
        when (it) {
          is Text -> {
            println("[Claude]: ${it.text}")
          }
          is ToolUse -> {
            println("[ToolUse]: $it")

            val result = if (autoConfirmToolUse) {
              it.use()
            } else {
              println("[ToolUse]: Can I use this tool? [yes/exit/or type a reason not to run it]")
              print("> ")
              when (val confirmLine = readln()) {
                "yes" -> it.use()
                "exit" -> return@runBlocking
                else -> ToolResult(
                  toolUseId = it.id,
                  "Human refused to run this command on their machine with the following reason: $confirmLine"
                )
              }
            }

            println("[ToolResult]: $result")
            toolResults += result
          }
          else -> println("Unexpected content type: $it")
        }
        println()
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
