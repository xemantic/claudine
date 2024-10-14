package com.xemantic.claudine.system

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val operatingSystem: String
  get() = Platform.osFamily.name
