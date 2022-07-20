import kotlinx.coroutines.runInterruptible
import java.io.File
import java.net.Proxy
import java.net.URL
import java.util.zip.GZIPInputStream

suspend fun main() {
  download(
    "https://mat1.gtimg.com/pingjs/ext2020/qqindex2018/dist/img/qq_logo_2x.png",
    File("./test")
  )
}
suspend fun download(
  urlStr: String,
  outputFile: File,
  proxy: Proxy? = null
) = runInterruptible {
  val oc = if (proxy == null) {
    URL(urlStr).openConnection()
  } else {
    URL(urlStr).openConnection(proxy)
  }
  val input = if (oc.contentEncoding == "gzip") {
    GZIPInputStream(oc.getInputStream())
  } else {
    oc.getInputStream()
  }
  outputFile.outputStream().buffered().use { output ->
    input.copyTo(output)
  }
}
