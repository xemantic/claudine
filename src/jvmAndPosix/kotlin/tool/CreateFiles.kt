package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Text
import com.xemantic.anthropic.message.ToolResult
import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.UsableTool
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlinx.io.writeString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@AnthropicTool("createFiles")
@Description("Creates files on human's machine.")
data class CreateFiles(
  val files: List<File>
) : UsableTool {

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun use(toolUseId: String): ToolResult {
    val errors = buildList {
      files.forEach { file ->
        try {
          file.writeFile()
        } catch (e : Exception) {
          add(file to e)
        }
      }
    }.map { (file, exception) ->
      Text("File creation error: $file, exception: $exception")
    }

    return ToolResult(
      toolUseId = toolUseId,
      content = if (errors.isEmpty()) {
        listOf(Text("All files created succesfully"))
      } else {
        errors
      }
    )
  }

  @Serializable
  @SerialName("file")
  data class File(
    @Description("The path of the file to write")
    val path: String,
    val content: String,
    @Description("Insertion point will be ignored for base64 encoded files")
    val insertionPoint: InsertionPoint? = null,
    @Description(
      "True if the file should be read as a base64 encoded string. " +
          "This might be useful for binary files." +
          "Defaults to false if not specified."
    )
    val base64: Boolean? = false
  )

  @Serializable
  @SerialName("insertionPoint")
  data class InsertionPoint(
    val line: Int,
    val column: Int
  )

  private fun File.writeFile() {
    val file = Path(path = path)
    if (insertionPoint == null) {
      writeNewFile(file)
    } else {
      updateExistingTextFile(file)
    }
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun File.writeNewFile(file: Path) {
    SystemFileSystem.sink(file).buffered().use { sink ->
      if (base64 == true) {
        sink.write(Base64.decode(content))
      } else {
        sink.writeString(content)
      }
    }
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun File.updateExistingTextFile(file: Path) {
    val lines = mutableListOf<String>()
    SystemFileSystem.source(file).buffered().use {
      do {
        val line = it.readLine()
        if (line != null) {
          lines.add(line)
        }
      } while (line != null)
    }

    if (insertionPoint!!.column > (lines.size + 1)) {
      throw IOException("There is no insertion point in the file")
    }

    var insertionLine = lines[insertionPoint.line - 1]
    if (insertionPoint.line > (insertionLine.length + 1)) {
      throw IOException("There is no insertion point in the file")
    }
    val builder = StringBuilder(insertionLine)
    builder.insert(insertionPoint.column, insertionPoint.line)
    lines[insertionPoint.line - 1] = builder.toString()
    val text = lines.joinToString("\n")

    SystemFileSystem.sink(file).buffered().use { sink ->
      sink.writeString(content)
    }
  }

}
