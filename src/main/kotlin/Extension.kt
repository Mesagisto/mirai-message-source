@file:Suppress("NOTHING_TO_INLINE")
package org.meowcat.mesagisto.mirai

import com.luciad.imageio.webp.WebPReadParam
import kotlinx.coroutines.runInterruptible
import org.meowcat.mesagisto.client.Db
import org.meowcat.mesagisto.client.Logger
import org.meowcat.mesagisto.client.toByteArray
import org.meowcat.mesagisto.client.toI32
import java.io.Closeable
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@JvmName("isWebp-ext")
suspend fun Path.isWebp() = isWebp(this)

suspend fun isWebp(path: Path): Boolean = runInterruptible fn@{
  Thread.currentThread().contextClassLoader = Plugin::class.java.classLoader
  runCatching {
    path.inputStream().use {
      val iis = ImageIO.createImageInputStream(it)
      val ir = ImageIO.getImageReaders(iis)
      val next = ir.next()
      when (next.formatName) {
        "WebP" -> true
        else -> false
      }
    }
  }
}.getOrThrow()
suspend fun convertWebpToPng(from: Path, to: Path) = runInterruptible {
  Thread.currentThread().contextClassLoader = Plugin::class.java.classLoader
  Logger.trace { "Converting image format" }
  // fixme use thread-local
  val reader = ImageIO.getImageReadersByMIMEType("image/webp").next()
  val readParam = WebPReadParam().apply {
    isBypassFiltering = true
  }
  reader.input = FileImageInputStream(from.toFile())
  val image = reader.read(0, readParam)
  to.outputStream().use {
    ImageIO.write(image, "png", it)
  }
  (reader.input as Closeable).close()
  Logger.trace { "Convert successfully" }
}

inline fun Db.putMsgId(
  target: Long,
  uid: Int,
  id: Int,
  reverse: Boolean = true
) = putMsgId(target.toByteArray(), uid.toByteArray(), id.toByteArray(), reverse)
inline fun Db.putMsgId(
  target: Long,
  uid: ByteArray,
  id: Int,
  reverse: Boolean = true
) = putMsgId(target.toByteArray(), uid, id.toByteArray(), reverse)

inline fun Db.getMsgId(
  target: Long,
  id: Int
): ByteArray? = getMsgId(target.toByteArray(), id.toByteArray())

inline fun Db.getMsgId(
  target: Long,
  id: ByteArray
): ByteArray? = getMsgId(target.toByteArray(), id)

inline fun Db.getMsgIdAsI32(
  target: Long,
  id: ByteArray
): Int? = getMsgId(target, id)?.toI32()
