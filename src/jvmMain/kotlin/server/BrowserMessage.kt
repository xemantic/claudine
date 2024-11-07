package com.xemantic.claudine.server

import com.xemantic.anthropic.content.Content
import com.xemantic.anthropic.content.ToolResult
import com.xemantic.anthropic.content.ToolUse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class InputMessage {

  @Serializable
  @SerialName("prompt")
  data class Prompt(
    val text: String
  ) : InputMessage()

  @Serializable
  @SerialName("toolUseConfirmation")
  data class TooUseConfirmation(
    val toolUseId: String,
    val confirmed: Boolean,
    val rejectionReason: String? = null
  ) : InputMessage()

}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class OutputMessage {

  @Serializable
  @SerialName("welcome")
  data class Welcome(
    val text: String
  ) : OutputMessage()

  @Serializable
  @SerialName("error")
  data class Error(
    val text: String
  ) : OutputMessage()

  @Serializable
  @SerialName("modelResponse")
  data class ModelResponse(
    val content: List<Content>
  ) : OutputMessage()

  @Serializable
  @SerialName("toolUseRequest")
  data class ToolUseRequest(
    val toolUse: ToolUse
  ) : OutputMessage()

  @Serializable
  @SerialName("toolUseOutput")
  data class ToolUseOutput(
    val toolResult: ToolResult
  ) : OutputMessage()

}
