package com.xemantic.ai.claudine

import platform.posix.execlp
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val operatingSystem: String
  get() = Platform.osFamily.name

actual fun ExecuteShellCommand.use(): String {

  execlp(command, command)
  return "foo"
}
