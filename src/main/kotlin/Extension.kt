package org.meowcat.mesagisto.mirai

import com.luciad.imageio.webp.WebPReadParam
import kotlinx.coroutines.runInterruptible
import org.meowcat.mesagisto.client.Logger
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

@JvmName("isWebp-ext")
suspend fun File.isWebp() = isWebp(this)

suspend fun isWebp(file: File): Boolean = runInterruptible fn@{
  Thread.currentThread().contextClassLoader = Plugin::class.java.classLoader
  val iis = ImageIO.createImageInputStream(file)
  val ir = ImageIO.getImageReaders(iis)
  val next = ir.next()
  return@fn when (next.formatName) {
    "WebP" -> true
    else -> false
  }
}
suspend fun convertWebpToPng(from: File, to: File) = runInterruptible {
  Thread.currentThread().contextClassLoader = Plugin::class.java.classLoader
  Logger.trace { "Converting image format" }
  val reader = ImageIO.getImageReadersByMIMEType("image/webp").next()
  val readParam = WebPReadParam().apply {
    isBypassFiltering = true
  }
  reader.input = FileImageInputStream(from)
  val image = reader.read(0, readParam)
  ImageIO.write(image, "png", to)
  Logger.trace { "Convert successfully" }
}
