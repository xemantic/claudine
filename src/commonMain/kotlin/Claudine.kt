/*
 * claudine - an autonomous and Unix-omnipotent AI agent using Anthropic API
 * Copyright (C) 2025 Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.claudine

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.AnthropicConfigException
import com.xemantic.ai.anthropic.Model
import com.xemantic.ai.anthropic.cache.CacheControl
import com.xemantic.ai.anthropic.cost.CostWithUsage
import com.xemantic.ai.anthropic.cost.costReport
import com.xemantic.ai.anthropic.cost.pricedBy
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.addCacheBreakpoint
import com.xemantic.ai.anthropic.message.plusAssign
import com.xemantic.ai.anthropic.message.toMessageResponse
import com.xemantic.ai.anthropic.thinking.ThinkingConfig
import com.xemantic.ai.anthropic.tool.Toolbox
import com.xemantic.ai.claudine.tool.CreateFile
import com.xemantic.ai.claudine.tool.ExecuteShellCommand
import com.xemantic.ai.claudine.tool.OpenUrl
import com.xemantic.ai.claudine.tool.ReadBinaryFiles
import com.xemantic.ai.claudine.tool.ReadFiles
import com.xemantic.ai.claudine.tool.describeTools
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.onEach

val claudineSystemPrompt = """
Your name is Claudine and you are an AI agent controlling the machine of the human you are connected to while using cognition of the Claude AI LLM model.

You are provided with tools to fulfill this purpose.

IMPORTANT: Always check file sizes before reading or processing them, especially for images and other potentially large files.

When reading files:
- First, use the ExecuteShellCommand tool to check the file size (e.g., `ls -l <filename>` or `stat -f%z <filename>`).
- For image and document formats supported by Claude models use ReadBinaryFiles tool.
- For image files larger than 5 KB:
   a. Use local image conversion tools to create a miniature version.
   b. The miniature should keep the aspect ratio of the original image and have a width of 512 pixels (e.g., `convert input.jpg -resize 512x temp_output.webp`).
   c. Use an image format with higher compression (e.g., WebP or JPEG) for the miniature.
   d. Store temporary files in the system's default temporary folder with unique names.

File operations:
- Prefer using shell commands (via ExecuteShellCommand) for copying or moving files instead of ReadFiles and CreateFile tools.
- When listing files with ExecuteShellCommand, use recursive lists with a maximum depth of 2 levels, to minimize the amount of excessive information quickly filling up the token window.

When analyzing the source code, work in 2 steps:
1. First establish the complete list of all the project source files using ExecuteShellCommand tool.
2. Then read all the text files at once using ReadFiles tool.

Always verify file sizes and types before processing, and never assume a file is small enough to read directly without checking first.

Your source code is located at:

git@github.com:xemantic/claudine.git

The operating system of human's machine: $operatingSystem

"""

/**
 * Starts claudine agent.
 *
 * @return the exit code
 */
suspend fun claudine(): Int {

    val model = Model.CLAUDE_OPUS_4_7
    val httpClient = HttpClient()

    val anthropic = try {
        Anthropic {
            anthropicBeta = listOf(
                "compact-2026-01-12",
                "task-budgets-2026-03-13"
            )
        }
    } catch (e: AnthropicConfigException) {
        println(e.message)
        return 1 // exit code
    }

    println("[Claudine]> Connecting human and human's machine to cognition of Claude AI")

    val systemPrompt = listOf(System(
        text = """
            $claudineSystemPrompt
            
            ${describeCurrentMoment()}
        """.trimIndent(),
        cacheControl = CacheControl.Ephemeral {
            ttl = CacheControl.Ephemeral.TTL.ONE_HOUR
        }
    ))

    val toolbox = Toolbox {
        tool<ExecuteShellCommand> { use() }
        tool<CreateFile> { use() }
        tool<ReadBinaryFiles> { use() }
        tool<ReadFiles> { use() }
        tool<OpenUrl> { use(httpClient) }
    }

    runAgent(anthropic, model, systemPrompt, toolbox)

    return 0 // exit code
}

private suspend fun runAgent(
    anthropic: Anthropic,
    model: Model,
    systemPrompt: List<System>,
    toolbox: Toolbox
) {

    var totalStats = CostWithUsage.ZERO
    val context = mutableListOf<Message>()

    while (true) {

        print("[me]> ")

        val input = readln()
        if (input == "exit") break

        context += input

        println("[Claudine] ...Reasoning...")

        var inAgentLoop = false
        do {

            if (inAgentLoop) {
                println("[Claudine] ...Processing tool results...")
            }

            var inThinking = false

            val response = anthropic.messages.stream {
                system = systemPrompt
                messages = context.addCacheBreakpoint()
                tools = toolbox.tools
                thinking = ThinkingConfig.Adaptive {
                    display = ThinkingConfig.Display.SUMMARIZED
                }
            }.onEach { event ->

                when (event) {

                    is ContentBlockStart -> {
                        val contentBlock = event.contentBlock
                        if (contentBlock is Thinking) {
                            inThinking = true
                            println("<thinking>")
                        }
                    }

                    is ContentBlockDelta -> {
                        when (val delta = event.delta) {
                            is TextDelta -> print(delta.text)
                            is ThinkingDelta -> print(delta.thinking)
                            else -> {} // nothing to do
                        }
                    }

                    is ContentBlockStop -> {
                        if (inThinking) {
                            println("\n<thinking>")
                            inThinking = false
                        }
                        println()
                    }

                    else -> { /* not printing other events */ }

                }
            }.toMessageResponse()

            if (response.stopReason == MAX_TOKENS) {
                println("[Claudine]> Error: max number of output tokens reached")
                context[context.lastIndex] = context.last().copy {
                    +"Limit the output not to exceed the limit of ${model.maxOutput} tokens"
                }
                continue
            }

            context += response

            val stats = response.usage.pricedBy(model)
            totalStats += stats

            reportCosts(stats, totalStats)

            inAgentLoop = response.stopReason == TOOL_USE
            if (inAgentLoop) {
                response.describeTools()
                context += response.useTools(toolbox)
            }

        } while (inAgentLoop)

    }
}

private fun reportCosts(
    stats: CostWithUsage,
    totalStats: CostWithUsage
) {
    println("[Claudine]> Tax:")
    print(costReport(stats, totalStats))
    println("|")
}
