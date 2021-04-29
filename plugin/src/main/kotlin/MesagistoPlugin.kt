package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registerCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.utils.info

object MesagistoPlugin : KotlinPlugin(
   JvmPluginDescription(
      id = "org.meowcat.mesagisto",
      name = "Mesagisto",
      version = "0.1.0"
   )
) {
   val eventChannel = globalEventChannel()
   val listener = eventChannel
      .subscribeAlways<GroupMessageEvent> {
         MessageHandler.handle(this)
      }
   override fun onEnable() {
      MesagistoConfig.reload()
      registerCommand(MesagistoCommand)
      logger.info { "信使插件已启用" }
   }

   override fun onDisable() {
      unregisterAllCommands(this)
      logger.info { "信使插件已禁用" }
   }
}
