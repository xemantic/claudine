package com.xemantic.claudine.system

actual val operatingSystem: String
  get() = System.getProperty("os.name")
