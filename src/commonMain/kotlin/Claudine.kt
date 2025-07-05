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
import com.xemantic.ai.anthropic.content.ToolResult
import com.xemantic.ai.anthropic.content.ToolUse
import com.xemantic.ai.anthropic.cost.Cost
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.StopReason
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.ToolResultMessageBuilder
import com.xemantic.ai.anthropic.message.addCacheBreakpoint
import com.xemantic.ai.anthropic.message.plusAssign
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.anthropic.usage.Usage
import com.xemantic.ai.claudine.tool.ClaudineTool
import com.xemantic.ai.claudine.tool.CreateFile
import com.xemantic.ai.claudine.tool.ExecuteShellCommand
import com.xemantic.ai.claudine.tool.OpenUrl
import com.xemantic.ai.claudine.tool.ReadBinaryFiles
import com.xemantic.ai.claudine.tool.ReadFiles
import com.xemantic.ai.claudine.tool.formatAsToolDescription
import io.ktor.client.HttpClient

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
 * @param autoConfirmToolUse auto confirms tools use if set to `true`, asks for confirmation every time otherwise.
 * @return the exit code
 */
suspend fun claudine(
    autoConfirmToolUse: Boolean,
): Int {

    val httpClient = HttpClient()

    val anthropic = try {
        Anthropic {
            anthropicBeta = listOf(
                "token-efficient-tools-2025-02-19",
                "prompt-caching-2024-07-31"
            )
            defaultModel = Model.CLAUDE_4_SONNET
            defaultMaxTokens = Model.CLAUDE_4_SONNET.maxOutput
        }
    } catch (e: AnthropicConfigException) {
        println(e.message)
        return 1 // exit code
    }

    println("[Claudine]> Connecting human and human's machine to cognition of Claude AI")

    val conversation = mutableListOf<Message>()
    var totalUsage = Usage.ZERO
    var totalCost = Cost.ZERO
    val systemPrompt = listOf(
        System(
            text = """
                $claudineSystemPrompt
                
                ${describeCurrentMoment()}
            """.trimIndent(),
            cacheControl = CacheControl.Ephemeral()
        )
    )

    val claudineTools = listOf(
        Tool<ExecuteShellCommand> { use() },
        Tool<CreateFile> { use() },
        Tool<ReadBinaryFiles> { use() },
        Tool<ReadFiles> { use() },
        Tool<OpenUrl> { use(httpClient) }
    )

    while (true) {

        print("[me]> ")

        val input = readln()
        if (input == "exit") break

        conversation += input

        println("[Claudine] ...Reasoning...")

        var agentLoop = false
        do {
            if (agentLoop) {
                println("[Claudine] ...Processing tool results...")
            }
            val response = anthropic.messages.create {
                system = systemPrompt
                // TODO this should go to the SDK
                messages = conversation.addCacheBreakpoint()
                tools = claudineTools
            }
            if (response.stopReason == StopReason.MAX_TOKENS) {
                println("[Claudine]> Error: max number of output tokens reached")
                conversation[conversation.lastIndex] = conversation.last().copy {
                    +"Limit the output not to exceed the limit of 64000 tokens"
                }
                continue
            }

            conversation += response

            response.text?.run {
                println("[Claudine]> ${response.text}")
            }

            totalUsage += response.usage
            val cost = Model.CLAUDE_4_SONNET.cost * response.usage
            totalCost += cost

            println("[Claudine]> Tax:")
            print(costReport(response.usage, totalUsage, cost, totalCost))
            println("|")

            val toolResultMessageBuilder = ToolResultMessageBuilder()
            response.content.filterIsInstance<ToolUse>().forEach {
                val toolInput = it.decodeInput() as ClaudineTool
                println("[Claudine]> I want to use ${it.name} tool: ${toolInput.purpose}")
                println(toolInput.info.formatAsToolDescription())

                val result = if (autoConfirmToolUse) {
                    it.use()
                } else {
                    println("[Claudine]> Can I use this tool? [yes/exit/or type a reason not to run it]")
                    print("[me]> ")
                    when (val confirmLine = readln()) {
                        "yes" -> it.use()
                        "exit" -> return 0 // exit code
                        else -> ToolResult {
                            toolUseId = it.id
                            isError = true
                            +"The human refused to run this command on their machine with the following reason: $confirmLine"
                        }
                    }
                }
                toolResultMessageBuilder.add(result)
            }

            agentLoop = response.stopReason == StopReason.TOOL_USE
            if (agentLoop) {
                conversation += toolResultMessageBuilder.build()
            }

        } while (agentLoop)

    }

    return 0 // exit code
}