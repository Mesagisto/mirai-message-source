package org.meowcat.mesagisto.mirai

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group
import org.mesagisto.client.Server
import java.util.UUID

object Config : AutoSavePluginConfig("config") {

  val bindings: MutableMap<Long, String> by value()

  val cipher: CipherConfig by value()

  val switch: SwitchConfig by value()

  val proxy: ProxyConfig by value()

  val blacklist: MutableList<Long> by value(mutableListOf(114514114514))

  @ValueName("disable_group")
  val disableGroup: MutableList<Long> by value(mutableListOf(114514114514))

  @ValueName("disable_channel")
  val disableChannel: MutableList<String> by value(mutableListOf("114514114514"))

  val perm: PermConfig by value()

  private val targetChannelMapper: MutableMap<Long, String> by value()

  @Serializable
  data class PermConfig(
    val strict: Boolean = false,
    val users: List<Long> = listOf(123456)
  )

  @Serializable
  data class ProxyConfig(
    val enable: Boolean = false,
    val address: String = "http://127.0.0.1:7890"
  )

  @Serializable
  data class CipherConfig(
    val key: String = "default"
  )

  @Serializable
  data class SwitchConfig(
    val nudge: Boolean = true
  )

  fun mapper(target: Long): String? = bindings[target]
  fun mapper(target: Group): String? = bindings[target.id]
  fun migrate() {
    bindings.putAll(targetChannelMapper)
    targetChannelMapper.clear()
  }
  fun roomAddress(target: Long): String? = bindings[target]

  fun roomId(target: Long): UUID? {
    val roomAddress = roomAddress(target) ?: return null
    return Server.roomId(roomAddress)
  }

  fun targetId(roomId: UUID): List<Long>? {
    val roomAddress = Server.roomMap.firstNotNullOfOrNull {
      if (it.value == roomId) it.key else null
    } ?: return null
    val targets = bindings.mapNotNull {
      if (it.value == roomAddress) it.key else null
    }
    return targets
  }
}
