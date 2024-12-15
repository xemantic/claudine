Some future ideas:

```kotlin
package com.xemantic.claudine.tool

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool
//import com.xemantic.claudine.Claudine
import kotlinx.serialization.Transient

@AnthropicTool("recursiveAgent")
@Description("Spawns recursive instance of claudine, itself with fresh system prompt, context window and task.")
data class RecursiveAgent(
  val systemPrompt: String,
  val initialMessage: String,
  val temperature:
) : UsableTool {

  @Transient
  lateinit var anthropic: Anthropic

  override suspend fun use(toolUseId: String): ToolResult {
    val claudine = Claudine(
      systemPrompt = systemPrompt
    )
    TODO("Not yet implemented")
  }

}

```

```kotlin
package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool
import com.xemantic.claudine.Claudine
import kotlinx.serialization.Transient

@AnthropicTool("systemPromptChanger")
@Description(
  "Changes the system prompt of the current conversation, while retaining " +
    "the same history of messages."
)
data class SystemPromptChanger(
  @Description("The new system prompt")
  val systemPrompt: String
) : UsableTool {

  @Transient
  lateinit var claudine: Claudine

  override suspend fun use(toolUseId: String): ToolResult {
    claudine.systemPrompt = systemPrompt
    TODO("Not yet implemented")
  }

}

```
