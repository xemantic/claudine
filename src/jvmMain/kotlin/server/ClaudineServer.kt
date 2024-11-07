package com.xemantic.claudine.server

import com.xemantic.anthropic.Anthropic
import com.xemantic.anthropic.anthropicJson
import com.xemantic.claudine.tool.CreateFile
import com.xemantic.claudine.tool.ExecuteShellCommand
import com.xemantic.claudine.tool.ReadBinaryFiles
import com.xemantic.claudine.tool.ReadFiles
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.origin
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration.Companion.seconds

fun main() {
  val server = ClaudineServer()
  runBlocking {
    server.startServer()
  }
}

class ClaudineServer() {

  private val logger = KotlinLogging.logger {}

  private val anthropic: Anthropic = Anthropic {
    anthropicBeta = "pdfs-2024-09-25,prompt-caching-2024-07-31"
    tool<ExecuteShellCommand>()
    tool<ReadFiles>()
    tool<CreateFile>()
    tool<ReadBinaryFiles>()
  }

//  private val sessionMap<String,
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
        webSocket("/ws") {

          val clientIp = call.request.origin.remoteAddress

          logger.info { "web socket connected: $clientIp" }

          try {

            val sender = object : OutputMessageSender {
              override suspend fun send(message: OutputMessage) {
                send(anthropicJson.encodeToString(OutputMessage.serializer(), message))
              }
            }

            sender.send(
              OutputMessage.Welcome(
                "You are connected to ClaudineServer, starting ClaudineSession"
              )
            )

            val claudineSession = ClaudineSession(anthropic)

            while (true) {
              val frame = incoming.receive()
              if (frame !is Frame.Text) {
                logger.error { "Received frame is not a text: ${frame.frameType}" }
                sender.send(OutputMessage.Error("Received frame is not a text: ${frame.frameType}"))
                continue
              }
              val receivedText = frame.readText()
              val message = try {
                anthropicJson.decodeFromString<InputMessage>(receivedText)
              } catch (e: SerializationException) {
                logger.error { "Unrecognized JSON message: $receivedText" }
                sender.send(OutputMessage.Error("Unrecognized JSON message: ${frame.frameType}"))
                continue
              }
              claudineSession.process(message, sender)
            }
          } catch (e: Exception) {
            logger.error {
              "Unexpected error in WebSocket session: ${e.message}"
            }
          }
        }
      }
    }

    server.start(wait = true)
  }


}