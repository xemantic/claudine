package com.xemantic.claudine.files

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlin.use

fun Path.toBytes(): ByteArray = SystemFileSystem.source(
  this
).buffered().use {
  it.readByteArray()
}

fun Path.readText() = SystemFileSystem.source(
  this
).buffered().use {
  it.readString()
}
