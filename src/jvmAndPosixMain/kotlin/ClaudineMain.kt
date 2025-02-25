package com.xemantic.ai.claudine

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {

    val autoConfirmToolUse = args.isNotEmpty() && args[0] == "-y"

    runBlocking {
        claudine(autoConfirmToolUse)
    }

}
