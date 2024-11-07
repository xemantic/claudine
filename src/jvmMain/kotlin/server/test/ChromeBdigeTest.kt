package com.xemantic.claudine.server.test

import kotlinx.coroutines.runBlocking

// bridge test
fun main() {
  val bridge = ChromeBridge()
  runBlocking {
    bridge.startServer()
  }
}
