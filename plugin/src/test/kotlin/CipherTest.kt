import io.ktor.util.hex
import org.meowcat.mesagisto.client.Cipher

object CipherTest {
  @JvmStatic fun main(args: Array<String>) {
    testCipher()
  }
  private fun testCipher() {
    Cipher.init("this is an example key")
    val hexCiphertext = hex(readLine()!!)
    val hexNonce = hex(readLine()!!)
    println(hex(Cipher.decrypt(hexCiphertext, hexNonce)))
  }
}
