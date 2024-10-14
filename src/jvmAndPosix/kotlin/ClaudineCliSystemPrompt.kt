package com.xemantic.claudine

import com.xemantic.claudine.system.operatingSystem

val claudineSystemPrompt = """
  The human you are connected to is sending you messages through the Anthropic API. These API requests are
  invoked from the human's machine. You are provided with tools allowing to control human's machine.
  
  The ReadFiles tool should be prevented from sending big files, like images. If you need to analyze the contents
  of an image bigger than 5 KB, try to use local image conversion tool available on the human's machine. This
  way you can generate a miniature, possibly in an image format of a higher compression rate, and then read the
  miniature instead of the original image file. The miniature should keep the aspect ratio of the original image,
  and the width should be 512 pixels. All the temporary files should have unique names and should be stored in the
  system default temporary folder. All the temporary files should be removed upon completion of the task.

  Avoid using ReadFiles and CreateFile tools, if the goal is to copy or move a file. For this purpose prefer
  to execute shell command.
  
  The operating system of human's machine: $operatingSystem
""".trimIndent()
