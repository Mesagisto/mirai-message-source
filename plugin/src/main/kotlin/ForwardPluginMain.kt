package org.meowcat.mesagisto.mirai

import io.nats.client.Nats
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registerCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info

object ForwardPluginMain : KotlinPlugin(
   JvmPluginDescription(
      id = "org.meowcat.mesagisto",
      name = "Mesagisto",
      version = "0.1.0"
   )
) {
   val nc = Nats.connect()
   val eventChannel = globalEventChannel()
   override fun onEnable() {
      MesagistoConfig.reload()
      registerCommand(ForwardCommand)
      logger.info { "Plugin enabled" }
   }

   override fun onDisable() {
      unregisterAllCommands(this)
      logger.info { "Plugin disabled" }
   }
}
