package org.meowcat.mesagisto
import io.ktor.util.hex
import org.junit.Test
import org.meowcat.mesagisto.client.Cipher

class CipherTest {

  @Test fun testCipher() {
    Cipher.init("this is an example key")
    val plaintext = "test".toByteArray()
    val hexNonce = Cipher.newNonce()
    println(hex(Cipher.encrypt(plaintext, hexNonce)))
  }
}
