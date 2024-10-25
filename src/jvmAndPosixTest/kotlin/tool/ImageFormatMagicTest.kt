package com.xemantic.claudine.tool

import com.xemantic.anthropic.message.Image
import io.kotest.matchers.shouldBe
import kotlinx.io.files.Path
import kotlin.test.Test

class ImageFormatMagicTest {

  @Test
  fun shouldDetectImageMediaType() {
    Path(
      "test-data/minimal.jpg"
    ).toBytes().maybeImageMediaType() shouldBe Image.MediaType.IMAGE_JPEG
    Path(
      "test-data/minimal.png"
    ).toBytes().maybeImageMediaType() shouldBe Image.MediaType.IMAGE_PNG
    Path(
      "test-data/minimal.gif"
    ).toBytes().maybeImageMediaType() shouldBe Image.MediaType.IMAGE_GIF
    Path(
      "test-data/minimal.webp"
    ).toBytes().maybeImageMediaType() shouldBe Image.MediaType.IMAGE_WEBP
  }

}
