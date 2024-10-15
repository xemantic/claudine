package com.xemantic.claudine

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.message.Message
import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.message.ToolUse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * TODO Work in progress - generic claudine not dependent on CLI.
 */
fun Flow<String>.claudine(
  client: Anthropic,
  systemPrompt: String
): Flow<String> = flow {

  val conversation = mutableListOf<Message>()

  collect { input ->
    conversation += Message { +input } // no cache involved

    var continueRequest: Boolean
    do {

      val response = client.messages.create {
        system(systemPrompt)
        messages = conversation
        maxTokens = 4096 * 2
        useTools()
      }

      conversation += response.asMessage()

      val toolResults = mutableListOf<ToolResult>()
      response.content.forEach {
        when (it) {
          is Text -> {
            emit("[Claude]: ${it.text}\n")
          }
          is ToolUse -> {
            emit("[ToolUse]: $it\n")
            val result = it.use()
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
