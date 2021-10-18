package org.meowcat.mesagisto.mirai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group

object Config : AutoSavePluginConfig("mesagisto") {
  val nats: NatsConfig by value()
  val cipher: CipherConfig by value()
  val format: FormatConfig by value()
  val behavior: BehaviorConfig by value()
  val targetChannelMapper: MutableMap<Long, String> by value()
  fun mapper(target: Long): String? = targetChannelMapper[target]
  fun mapper(target: Group): String? = targetChannelMapper[target.id]
}

@Serializable
data class NatsConfig(
  val address: String = "nats://itsusinn.site:4222"
)

@Serializable
data class CipherConfig(
  var enable: Boolean = true,
  val key: String = "this is an example key"
)
@Serializable
data class BehaviorConfig(
  @SerialName("lan-first-when-handle-image")
  var lanFirstWhenHandleImage: Boolean = true,
  @SerialName("omit-sender-if-same")
  var omitSenderIfSame: Boolean = true,
  @SerialName("omit-image")
  var omitImage: Boolean = true
)

@Serializable
data class FormatConfig(
  @SerialName("text-style")
  var textStyle: String = "",
  @SerialName("text-style-without-sender")
  var textStyleWithoutSender: String = ""
)
