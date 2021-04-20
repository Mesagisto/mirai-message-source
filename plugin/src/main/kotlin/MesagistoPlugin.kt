package org.meowcat.mesagisto.mirai

import io.nats.client.Nats
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registerCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info

object MesagistoPlugin : KotlinPlugin(
   JvmPluginDescription(
      id = "org.meowcat.mesagisto",
      name = "Mesagisto",
      version = "0.1.0"
   )
) {
   val nc = Nats.connect(MesagistoConfig.address)
   val eventChannel = globalEventChannel()
   val listen = eventChannel.subscribeAlways<GroupMessageEvent> {

   }
   override fun onEnable() {
      MesagistoConfig.reload()
      registerCommand(MesagistoCommand)
      logger.info { "Plugin enabled" }
   }

   override fun onDisable() {
      unregisterAllCommands(this)
      logger.info { "Plugin disabled" }
   }
}
