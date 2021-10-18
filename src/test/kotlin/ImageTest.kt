import com.luciad.imageio.webp.WebPReadParam
import kotlinx.coroutines.runInterruptible
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

suspend fun main() {
  convertWebpToJpg(File("AgADaQADFbnXJA"), File("dd"))
}

suspend fun isWebp() = runInterruptible {
  val file = File("AgADaQADFbnXJA")
  val iis = ImageIO.createImageInputStream(file)
  val ir = ImageIO.getImageReaders(iis)
  val next = ir.next()
  when (next.formatName) {
    "WebP" -> { }
    else -> {}
  }
}

suspend fun convertWebpToJpg(from: File, to: File) = runInterruptible {
  val reader = ImageIO.getImageReadersByMIMEType("image/webp").next()
  val readParam = WebPReadParam().apply {
    isBypassFiltering = true
  }
  reader.input = FileImageInputStream(from)
  val image = reader.read(0, readParam)
  ImageIO.write(image, "jpg", to)
}
