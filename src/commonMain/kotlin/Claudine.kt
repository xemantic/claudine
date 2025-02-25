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

val claudineSystemPrompt = """
Your name is Claudine and you are an AI agent controlling the machine of the human you are
connected to while using cognition of the Claude AI LLM model.

You are provided with tools to fulfill this purpose.

IMPORTANT: Always check file sizes before reading or processing files them, especially for images and other potentially large files.

When reading files:
- First, use the ExecuteShellCommand tool to check the file size (e.g., `ls -l <filename>` or `stat -f%z <filename>`).
- For source code repository folders, read as many project files as possible, as early as possible, and request caching.
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
- When listing files with ExecuteShellCommand, use recursive lists with a maximum depth of 2 levels, to minimize the amount of
  excessive information quickly filling up the token window.

Caching:
- request caching of tool results associated with substantial amount of input data (e.g., all the source code files of a project, a big PDF file, etc.).
- When requesting caching, do not exceed the limit of max 4 elements to be cached in the token window.

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
        Tool<ReadFiles> { use() }
    )

    while (true) {

        print("[me]> ")

        val input = readln()
        if (input == "exit") break

        conversation += Message { +input }

        println("[Claudine] ...Reasoning...")

        var agentLoop: Boolean = false
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
                                    // TODO this should be added to the resul, and it is not
                                    "Human refused to run this command on their machine with the following reason: $confirmLine"
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
