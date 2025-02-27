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
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.content.ToolResult
import com.xemantic.ai.anthropic.content.ToolUse
import com.xemantic.ai.anthropic.error.AnthropicException
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.plusAssign
import com.xemantic.ai.anthropic.tool.Tool
import io.ktor.client.HttpClient

val claudineSystemPrompt = """
Your name is Claudine and you are an AI agent controlling the machine of the human you are connected to while using cognition of the Claude AI LLM model.

You are provided with tools to fulfill this purpose.

IMPORTANT: Always check file sizes before reading or processing them, especially for images and other potentially large files.

When opening URLs:
- If URL is expected to return HTML, always prefix it with https://r.jina.ai/ so that the Markdown is returned instead of HTML

When reading files:
- First, use the ExecuteShellCommand tool to check the file size (e.g., `ls -l <filename>` or `stat -f%z <filename>`).
- For image and document formats supported by Claude models use ReadBinaryFiles tool.
- For image files larger than 5 KB:
   a. Use local image conversion tools to create a miniature version.
   b. The miniature should keep the aspect ratio of the original image and have a width of 512 pixels (e.g., `convert input.jpg -resize 512x temp_output.webp`).
   c. Use an image format with higher compression (e.g., WebP or JPEG) for the miniature.
   d. Store temporary files in the system's default temporary folder with unique names.

When writing files:
- Consider requesting multiple CreateFile tool uses at once, so that many files establishing a complete project can be created at once.
- Switch to requesting individual CreateFile tool uses if the files to create are big and might exceed the max amount of output tokens.

File operations:
- Prefer using shell commands (via ExecuteShellCommand) for copying or moving files instead of ReadFiles and CreateFile tools.
- When listing files with ExecuteShellCommand, use recursive lists with a maximum depth of 2 levels, to minimize the amount of excessive information quickly filling up the token window.

When analyzing the source code, work in 2 steps:
1. First establish the complete list of all the project source files using ExecuteShellCommand tool.
2. Then read all the text files at once using ReadFiles tool.

Always verify file sizes and types before processing, and never assume a file is small enough to read directly without checking first.

The operating system of human's machine: $operatingSystem
"""

fun environmentContextSystemPrompt() = """
Current date: ${describeCurrentMoment()}
"""

fun systemPrompt() = listOf(
    claudineSystemPrompt,
    environmentContextSystemPrompt()
).map {
    System(text = it)
}

suspend fun claudine(
    autoConfirmToolUse: Boolean,
) {

    println("[Claudine]> Connecting human and human's machine to cognition of Claude AI")

    val httpClient = HttpClient()

    val anthropic = try {
        Anthropic()
    } catch (e: AnthropicException) {
        println(e.message) // most likely we don't have ANTHROPIC_API_KEY
        return
    }

    val conversation = mutableListOf<Message>()
    val cacheManager = CacheManager()
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

        conversation += Message { +input }

        println("[Claudine] ...Reasoning...")

        var agentLoop = false
        do {
            if (agentLoop) {
                println("[Claudine] ...Processing tool results...")
            }
            val response = anthropic.messages.create {
                system = systemPrompt()
                messages = conversation
                maxTokens = 4096 * 2 // for the latest model
                tools = claudineTools
                // TODO it should be taken from the default which is not passed in Anthropic now, we need unit tests for this
            }

            conversation += response

            val toolResults = mutableListOf<ToolResult>()
            response.content.forEach {
                when (it) {
                    is Text -> {
                        println("[Claudine]> ${it.text}")
                    }

                    is ToolUse -> {
                        println("[Claudine]> I want to use ${it.name} tool")
                        println(getTooUseInfo(it.decodeInput()))

                        val result = if (autoConfirmToolUse) {
                            it.use()
                        } else {
                            println("[Claudine]> Can I use this tool? [yes/exit/or type a reason not to run it]")
                            print("[me]> ")
                            when (val confirmLine = readln()) {
                                "yes" -> it.use()
                                "exit" -> return
                                else -> ToolResult {
                                    toolUseId = it.id
                                    isError = true
                                    +"The human refused to run this command on their machine with the following reason: $confirmLine"
                                }
                            }
                        }
                        toolResults += result
                    }

                    else -> println("[Error]: Unexpected content type: $it")
                }
            }

            agentLoop = toolResults.isNotEmpty()
            if (agentLoop) {
                conversation += Message {
                    content += toolResults
                }
            }

        } while (agentLoop)

    }

}

class CacheManager(
    numberOfCachableElements: Int = 4 // depends on the model
) {

    var permissions: Int = numberOfCachableElements

    fun canCache(): Boolean =
        if (permissions == 0) false
        else {
            permissions--
            true
        }

}
