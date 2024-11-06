package com.xemantic.claudine.tool

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.ToolInput
import kotlinx.serialization.Transient

// TODO work in progress
@AnthropicTool("RecursiveSession")
@Description(
  "Creates a recursive session, starting a new conversation with LLM, with a fresh token window. " +
      "It allows to conduct a subtask in isolation. " +
      "The new session will be able to use all the tools available to the parent session. " +
      "If the response from the LLM is not containing tool use requests anymore, the session" +
      "will be closed and the latest text content will be passed back to the parent session"
)
data class RecursiveSession(
  @Description("The initial message to trigger a subtask execution.")
  val initialMessage: String,
  @Description("The LLM can decide on adding particular instruction in addition to existing system prompt.")
  val additionalSystemPrompt: String? = null
) : ToolInput() {

  @Transient
  internal lateinit var anthropic: Anthropic

  init {
    use {
      "TODO Recursive session still to be implemented"
    }
  }

}
