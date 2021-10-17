package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registerCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.info
import org.meowcat.mesagisto.client.Cipher
import org.meowcat.mesagisto.client.Db
import org.meowcat.mesagisto.client.Res
import org.meowcat.mesagisto.client.Server
import org.meowcat.mesagisto.mirai.handlers.MiraiListener

object Plugin : KotlinPlugin(
  JvmPluginDescription(
    id = "org.meowcat.mesagisto",
    name = "Mesagisto",
    version = "1.0.0"
  )
) {
  private val eventChannel = globalEventChannel()
  private val listener =
    eventChannel.subscribeAlways(MiraiListener::handle)
  override fun onEnable() {

    Config.reload()
    registerCommand(Command)
    Db.init("mirai")
    Server.initNC(Config.nats.address)
    Cipher.init(Config.cipher.key)
    Res.resolvePhotoUrl { uid, _ ->
      runCatching {
        val image = Image(uid)
        image.queryUrl()
      }
    }
    logger.info { "信使插件已启用" }
  }

  override fun onDisable() {
    unregisterAllCommands(this)
    listener.complete()
    logger.info { "信使插件已禁用" }
  }
}
