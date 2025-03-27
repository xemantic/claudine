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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.claudine.tool

import com.xemantic.ai.anthropic.content.Content
import com.xemantic.ai.anthropic.content.Image
import com.xemantic.ai.anthropic.content.Source
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.tool.schema.meta.Description
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName

@SerialName("OpenUrl")
@Description("Reads contents of an URL")
data class OpenUrl(
    @Description("The purpose of opening this URL")
    override val purpose: String,
    val url: String,
) : ClaudineTool {

    suspend fun use(client: HttpClient): Content {
        val head = client.head(url)
        val contentType = head.contentType()
        return if (contentType != null) {
            when {
                contentType.match(ContentType.Text.Html) ->
                    Text(client.get("https://r.jina.ai/$url").bodyAsText())
                contentType.match(
                    ContentType.Text.Any,
                    ContentType.Image.SVG,
                    ContentType.Application.Json,
                    ContentType.Application.Xml
                ) -> Text(
                    client.get(url).bodyAsText()
                )
                contentType.match(
                    ContentType.Image.PNG,
                    ContentType.Image.JPEG,
                    ContentType.Image.GIF,
                    ContentType.Image.WEBP
                ) -> Image {
                    source = Source.Url(url)
                }
                else -> client.get(url).bodyAsBytes().toContent()
            }
        } else {
            client.get(url).bodyAsBytes().toContent()
        }
    }

    override val info get() = url

}

private val ContentType.Image.WEBP get() = ContentType("image", "webp")

private fun ContentType.match(
    vararg contentTypes: ContentType
) = contentTypes.any {
    this.match(it)
}
