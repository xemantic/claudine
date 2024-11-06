package com.xemantic.claudine.tool

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@Serializable
data class MarkdownResponse(
  val url: String,
  val content: String
)

@Serializable
data class ChromeMessage(
  val type: String,
  val url: String? = null,
  val content: String? = null
)

class ChromeBridge {

  private val json = Json { ignoreUnknownKeys = true }
  private lateinit var process: Process
  private var serverJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Default + Job())

  private val markdownFlow = MutableSharedFlow<String>()

  suspend fun startServer() {

    val server = embeddedServer(CIO, 8080) {
      install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
      }
      routing {
        post("/markdown") {
          val markdown = call.receive<String>()
          markdownFlow.emit(markdown)
          call.respond(HttpStatusCode.OK, "Received")
        }
        webSocket("/claudine") {
          send("Please enter your name")
          for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val receivedText = frame.readText()
            if (receivedText.equals("bye", ignoreCase = true)) {
              close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
            } else {
              send(Frame.Text("Hi, $receivedText!"))
            }
          }
        }
      }
    }

      server.start(wait = true)

    //return server.environment.config.port
  }

  suspend fun open(url: String): String {
    sendMessageToChrome(
      ChromeMessage(type = "navigate", url = url)
    )
    return markdownFlow.first()
  }

  private fun sendMessageToChrome(message: ChromeMessage) {
    // Using Chrome Native Messaging protocol
    //val messageJson = json.encodeToString(message)
    //val messageBytes = messageJson.toByteArray()

//    System.out.buffer.apply {
//      write(messageBytes.size.toBigInteger().toByteArray())
//      write(messageBytes)
//      flush()
//    }
  }

  suspend fun start() {
    try {
      startServer()
    } finally {
      cleanup()
    }
  }

  private fun cleanup() {
    serverJob?.cancel()
    process.destroy()
    scope.cancel()
  }
}


fun main() {
  val bridge = ChromeBridge()
  runBlocking {
    bridge.startServer()
  }
}

//// Extension function to read Chrome native messaging input
//fun InputStream.readChromeMessage(json: Json): ChromeMessage? {
//  try {
//    // Read message length (4 bytes)
//    val lengthBytes = ByteArray(4)
//    if (read(lengthBytes) != 4) return null
//
//    val length = lengthBytes.fold(0) { acc, byte ->
//      (acc shl 8) + (byte.toInt() and 0xFF)
//    }
//
//    // Read message content
//    val messageBytes = ByteArray(length)
//    if (read(messageBytes) != length) return null
//
//    return json.decodeFromString<ChromeMessage>(messageBytes.toString(Charsets.UTF_8))
//  } catch (e: Exception) {
//    println("Error reading Chrome message: ${e.message}")
//    return null
//  }
//}
//
