/*
 * claudine - an autonomous and Unix-omnipotent AI agent using Anthropic API
 * Copyright (C) 2025 Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.xemantic.ai.claudine

import com.xemantic.ai.claudine.tool.TranscribeAudio
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

fun main() = runBlocking {
    // Check if API key is set
    val apiKey = System.getenv("ELEVENLABS_API_KEY")
    if (apiKey == null) {
        println("Error: ELEVENLABS_API_KEY environment variable is not set.")
        println("Please set it using: export ELEVENLABS_API_KEY=your_key_goes_here")
        return@runBlocking
    }
    
    // Create a temporary directory for downloads if it doesn't exist
    val tempDir = File(System.getProperty("java.io.tmpdir"), "claudine-audio-test")
    tempDir.mkdirs()
    
    // Download a public domain audio file
    // This is a sample from LibriVox (public domain)
    val audioUrl = "https://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bd/_-0S/_ANd5-kg571S/c12310fe-acb7-4b33-bfed-b70f806a14de_2.mp3"
    val tempFile = File(tempDir, "sample_audio.mp3")
    val tempFilePath = tempFile.absolutePath
    
    println("Downloading audio sample from $audioUrl")
    println("Saving to $tempFilePath")
    
    // Download the file
    val client = HttpClient()
    val response = client.get(audioUrl)
    val channel = response.bodyAsChannel()
    
    // Save it to the temp file
    val path = Path(tempFilePath)
    SystemFileSystem.sink(path).buffered().use { sink ->
        channel.toInputStream().use { input ->
            sink.write(input.readBytes())
        }
    }
    
    println("Download complete")
    println("File size: ${tempFile.length()} bytes")
    
    // Create the transcription tool and use it
    println("\nTranscribing audio...")
    val transcriber = TranscribeAudio(
        audioPath = tempFilePath,
        purpose = "Testing audio transcription",
        languageCode = "en" // English
    )
    
    try {
        val transcription = transcriber.use(client)
        println("\nTranscription result:\n-------------------")
        println(transcription)
        println("-------------------")
    } catch (e: Exception) {
        println("Error during transcription: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
    }
    
    println("\nTest complete")
    println("The temporary file can be found at: $tempFilePath")
}