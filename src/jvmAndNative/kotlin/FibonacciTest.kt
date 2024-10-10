package com.xemantic.claudine

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.message.Message
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.message.ToolUse
import com.xemantic.anthropic.tool.UsableTool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class Fibonacci(val n: Int) : UsableTool {

  override fun use(
    toolUseId: String
  ) = ToolResult(toolUseId, "${fibonacci(n)}")

  tailrec fun fibonacci(
    n: Int, a: Int = 0, b: Int = 1
  ): Int = when (n) {
    0 -> a; 1 -> b; else -> fibonacci(n - 1, b, a + b)
  }

}

fun main() = runBlocking {
  val client = Anthropic { tool<Fibonacci>() }
  val response = client.messages.create {
    +Message { +"What's Fibonacci number 42" }
    useTools()
  }
  val toolUse = response.content.filterIsInstance<ToolUse>().first()
  val toolResult = toolUse.use()
  println(toolResult)
}
