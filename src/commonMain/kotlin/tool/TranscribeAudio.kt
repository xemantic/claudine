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

package com.xemantic.ai.claudine.tool

import com.xemantic.ai.tool.schema.meta.Description
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@SerialName("TranscribeAudio")
@Description("Transcribes audio files to text using ElevenLabs API")
data class TranscribeAudio(
    @Description("The path to the audio file")
    val audioPath: String,
    @Description("The purpose of transcribing this audio")
    override val purpose: String,
    @Description("Optional language code (e.g., 'en', 'fr', 'de') - if omitted, ElevenLabs will auto-detect")
    val languageCode: String? = null
) : ClaudineTool {

    suspend fun use(client: HttpClient): String {
        val apiKey = System.getenv("ELEVENLABS_API_KEY")
            ?: throw IllegalStateException("ELEVENLABS_API_KEY environment variable is not set.")

        val audioBytes = Path(audioPath).toBytes()

        // Query parameters
        val langParam = languageCode?.let { "&language_code=$it" } ?: ""
        
        val response = client.submitFormWithBinaryData(
            url = "https://api.elevenlabs.io/v1/speech-to-text?model_id=eleven_multilingual_v2&optimize_streaming_latency=0$langParam",
            formData = formData {
                append("file", audioBytes, Headers.build {
                    append(HttpHeaders.ContentType, "audio/mpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"audio.mp3\"")
                })
                append("model_id", "scribe_v1")
            }
        ) {
            headers {
                append("xi-api-key", apiKey)
                append("Accept", "application/json")
            }
        }

        if (response.status != HttpStatusCode.OK) {
            return "Error: ${response.status}\n${response.bodyAsText()}"
        }

        // Parse the JSON response
        val jsonResponse = Json.decodeFromString<ElevenLabsTranscriptionResponse>(response.bodyAsText())
        return jsonResponse.text
    }

    override val info: String get() = "Audio file: $audioPath"
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ElevenLabsTranscriptionResponse(
    val text: String,
    val language: String? = null,
    @SerialName("language_code")
    val languageCode: String? = null,
    @SerialName("detected_language")
    val detectedLanguage: String? = null
)