package com.xemantic.claudine.tool

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.use

fun Path.toBytes(): ByteArray = SystemFileSystem.source(this).buffered().use {
  it.readByteArray()
}
