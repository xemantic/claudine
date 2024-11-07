package com.xemantic.claudine.server.test

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
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

  // Keep track of all sessions
  private val sessions = mutableSetOf<DefaultWebSocketSession>()

  suspend fun startServer() {
    val server = embeddedServer(CIO, 8080) {
      install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
      }

      routing {
        // Serve static files from the web directory
        staticFiles(
          dir = File("src/web"),
          remotePath = "/"
        )
        post("/markdown") {
          val markdown = call.receive<String>()
          markdownFlow.emit(markdown)
          call.respond(HttpStatusCode.OK, "Received")
        }

        webSocket("/ws") {
          try {
            sessions.add(this)
//            send(Frame.Text("Connected to Claudine WebSocket Server"))

            for (frame in incoming) {
              frame as? Frame.Text ?: continue
              val receivedText = frame.readText()

              // Broadcast message to all connected clients
              sessions.forEach { session ->
                if (session != this) {
                  //send(json.encodeToString(Pong("bar")))
                  //send("""{"received": "$receivedText"}"""")
//                  session.send(Frame.Text("User message: $receivedText"))
                }
              }

              // Echo back to sender

//              send(json.encodeToString(Pong("bar")))
//              send("""{"received": "$receivedText"}"""")
            }
          } catch (e: Exception) {
            println("Error in WebSocket session: ${e.message}")
          } finally {
            sessions.remove(this)
          }
        }
      }
    }

    server.start(wait = true)
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

    //System.out.buffer.apply {
    //  write(messageBytes.size.toBigInteger().toByteArray())
    //  write(messageBytes)
    //  flush()
    //}
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

  // Broadcast a message to all connected WebSocket clients
  suspend fun broadcast(message: String) {
    sessions.forEach { session ->
      session.send(Frame.Text(message))
    }
  }
}

