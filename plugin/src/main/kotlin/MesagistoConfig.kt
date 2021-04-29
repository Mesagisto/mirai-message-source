package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object MesagistoConfig : AutoSavePluginConfig("mesagisto") {

   val address: String by value("nats://itsusinn.site:4222")

   val targetChannelMapper: MutableMap<Long, String> by value()
}
