package com.xemantic.claudine

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.content.ToolResult
import com.xemantic.ai.anthropic.content.ToolUse
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.plusAssign

suspend fun claudine(
  anthropic: Anthropic,
  systemPrompt: String, // TODO there should be default one in Anthropic in the future
  autoConfirmToolUse: Boolean
) {

  println("Connecting human and human's machine to Claude AI")

  val conversation = mutableListOf<Message>()

  while (true) {
    print("> ")
    val line = readln()
    if (line == "exit") { break }

    conversation += Message { +line } // no cache involved

    var continueRequest: Boolean
    do {
      print("[Claude] ...Reasoning...")
      val response = anthropic.messages.create {
        system(systemPrompt)
        system = listOf(
          systemPrompt,
          currentTimeSystemPrompt()
        ).map {
          System(text = it)
        }
        messages = conversation
        maxTokens = 4096 * 2 // for the latest model
        // TODO it should be taken from the default which is not passed in Anthropic now, we need unit tests for this
        allTools()
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
            println(">>> $it")

            val result = if (autoConfirmToolUse) {
                it.use()
            } else {
              println(">>> Can I use this tool? [yes/exit/or type a reason not to run it]")
              print("> ")
              when (val confirmLine = readln()) {
                "yes" -> it.use()
                "exit" -> return
                else -> ToolResult {
                  toolUseId = it.id
                  isError = true
                  // TODO this should be added to the resul, and it is not
                  "Human refused to run this command on their machine with the following reason: $confirmLine"
                }
              }
            }
            println()
            //println("<<< $result")
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

