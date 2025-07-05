package com.xemantic.ai.claudine

import com.xemantic.ai.anthropic.cost.Cost
import com.xemantic.ai.anthropic.usage.Usage

class Table {

    private val rows = mutableListOf<List<String>>()

    fun row(vararg any: Any) {
        rows += any.map { it.toString() }
    }

    override fun toString() = buildString {
        val maxLengths: List<Int> = rows[0].mapIndexed { index, item ->
            rows.maxOf {
                it[index].length
            }
        }
        rows.forEachIndexed { rowIndex, row ->
            append("| ")
            row.forEachIndexed { index, column ->
                val max = maxLengths[index]
                append(
                    if (index == 0 || rowIndex == 0) {
                        column.padEnd(max)
                    } else {
                        column.padStart(max)
                    }
                )
                append(" |")
                if (index < row.lastIndex) append(" ")
            }
            if (rowIndex == 0) {
                append("\n")
                append("|")
                maxLengths.forEach {
                    append("-".repeat(it + 2))
                    append("|")
                }
            }
            append("\n")
        }
    }

}

fun costReport(
    usage: Usage,
    totalUsage: Usage,
    cost: Cost,
    totalCost: Cost
) = Table().apply {
    row("",
        "request tokens",
        "total tokens",
        "request cost",
        "total cost"
    )
    row(
        "input",
        usage.inputTokens,
        totalUsage.inputTokens,
        "$${cost.inputTokens}",
        "$${totalCost.inputTokens}"
    )
    row(
        "output",
        usage.outputTokens,
        totalUsage.outputTokens,
        "$${cost.outputTokens}",
        "$${totalCost.outputTokens}",
    )
    row(
        "cache write",
        usage.cacheCreationInputTokens ?: 0,
        totalUsage.cacheCreationInputTokens ?: 0,
        "$${cost.cacheCreationInputTokens}",
        "$${totalCost.cacheCreationInputTokens}"
    )
    row(
        "cache read",
        usage.cacheReadInputTokens ?: 0,
        totalUsage.cacheReadInputTokens ?: 0,
        "$${cost.cacheReadInputTokens}",
        "$${totalCost.cacheReadInputTokens}"
    )
    row("", "", "", "", "")
    row(
        "window",
        "${usage.inputTokens + usage.outputTokens + (usage.cacheCreationInputTokens ?: 0) + (usage.cacheReadInputTokens ?: 0)}",
        "",
        "$${cost.total}",
        "$${totalCost.total}"
    )
}.toString()
