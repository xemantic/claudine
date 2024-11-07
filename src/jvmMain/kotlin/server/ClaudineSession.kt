package com.xemantic.claudine.server

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.content.Text
import com.xemantic.anthropic.content.ToolResult
import com.xemantic.anthropic.content.ToolUse
import com.xemantic.anthropic.message.Message
import com.xemantic.anthropic.message.StopReason
import com.xemantic.anthropic.message.System
import com.xemantic.anthropic.message.plusAssign
import com.xemantic.claudine.claudineSystemPrompt
import io.github.oshai.kotlinlogging.KotlinLogging

private val claudineHackathonSystemPrompt = """
Whenever you are planning to use tools, please visualize the process
in the form of a mermaid diagram. In particular include:
-tools being used
-resources operated on
-actors involved in the process

Prefer sequence diagrams.

IMPORTANT: The mermaid diagram should be included as a standalone text content
element in the response. The standard textual explanation should be included as well.
"""

interface OutputMessageSender {

  suspend fun send(message: OutputMessage)

}

class ClaudineSession(
  val anthropic: Anthropic
) {

  private val logger = KotlinLogging.logger {}

  val conversation = mutableListOf<Message>()

  suspend fun prompt(
    prompt: String,
    sender: OutputMessageSender
  ) {
    conversation += Message { +prompt }
    do {

      val response = anthropic.messages.create {
        messages = conversation
        allTools()
        system = listOf(
          System(text = claudineSystemPrompt),
          System(text = claudineHackathonSystemPrompt)
        )
        maxTokens = 4096 * 2 // for the latest model
      }

      conversation += response

      sender.send(
        OutputMessage.ModelResponse(
          content = response.content
        )
      )

      val toolResults = mutableListOf<ToolResult>()
      response.content.forEach {
        when (it) {
          is Text -> {
            logger.info {"[Claude]: ${it.text}" }
          }
          is ToolUse -> {
            logger.info {"[Claude]: $it" }
            sender.send(OutputMessage.ToolUseRequest(it))
            val result = it.use()
            toolResults += result
            sender.send(OutputMessage.ToolUseOutput(result))
          }
          else -> println("Unexpected content type: $it")
        }
      }

      conversation += Message { +toolResults }

    } while (response.stopReason == StopReason.TOOL_USE)

  }

  suspend fun process(
    message: InputMessage,
    sender: OutputMessageSender
  ) = when (message) {
    is InputMessage.Prompt -> {
      prompt(message.text, sender)
    }
    else -> {
      logger.error { "Internal error, don't know how to respond to: $message" }
      sender.send(
        OutputMessage.Error(
          text = "Internal error, don't know how to respond to message"
        )
      )
    }
  }

}
