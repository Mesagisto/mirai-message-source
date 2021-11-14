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
import org.meowcat.mesagisto.client.*
import org.meowcat.mesagisto.mirai.handlers.MiraiListener

object Plugin : KotlinPlugin(
  JvmPluginDescription(
    id = "org.meowcat.mesagisto",
    name = "Mesagisto",
    version = "1.0.0"
  )
) {
  private val eventChannel = globalEventChannel()
  private val listener = eventChannel.subscribeAlways(MiraiListener::handle)

  override fun onEnable() {
    Logger.bridgeToMirai(logger)
    Config.reload()

    if (Config.cipher.enable) {
      Cipher.init(Config.cipher.key, Config.cipher.refusePlain)
    } else {
      Cipher.deinit()
    }

    Db.init("mirai")
    Server.initNC(Config.nats.address)
    Res.resolvePhotoUrl { uid, _ ->
      runCatching {
        val image = Image(uid.toString(charset = Charsets.UTF_8))
        image.queryUrl()
      }
    }
    registerCommand(Command)
    logger.info { "信使插件已启用" }
  }

  override fun onDisable() {
    unregisterAllCommands(this)
    listener.complete()
    logger.info { "信使插件已禁用" }
  }
}
