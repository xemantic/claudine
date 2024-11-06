package com.xemantic.claudine.tool

import com.xemantic.anthropic.schema.Description
import com.xemantic.anthropic.tool.AnthropicTool
import com.xemantic.anthropic.tool.ToolInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val chromeBridge by lazy {
  ChromeBridge()
}

@AnthropicTool("BrowseWeb")
@Description("Browses the web")
class BrowseWeb(
  val url: String
) : ToolInput() {

  init {
    use {// TODO work in progress
//      CoroutineScope(Dispatchers.IO).launch {
//        val markdown = chromeBridge.open(url)
//        +markdown
//      }
    }
  }

}
