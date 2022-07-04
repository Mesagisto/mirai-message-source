package org.meowcat.mesagisto.mirai

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.fusesource.leveldbjni.internal.NativeDB
import org.meowcat.mesagisto.client.*
import org.meowcat.mesagisto.mirai.handlers.MiraiListener
import org.meowcat.mesagisto.mirai.handlers.Receive
import javax.imageio.ImageIO

object Plugin : KotlinPlugin(
  JvmPluginDescription(
    id = "org.meowcat.mesagisto",
    name = "Mesagisto",
    version = "1.0-unknown"
  )
) {
  private val eventChannel = globalEventChannel()
  private lateinit var listener: Listener<GroupMessageEvent>
  private lateinit var commandsListener: Listener<GroupMessageEvent>

  override fun onEnable() {
    Config.reload()
    Config.migrate()
    if (!Config.enable) {
      logger.warning("Mesagisto信使未启用!")
      return
    }
    // SPI And JNI related things
    switch(jvmPluginClasspath.pluginClassLoader) {
      ImageIO.scanForPlugins()
      NativeDB.LIBRARY.load()
      Result.success(Unit)
    }.getOrThrow()

    Logger.bridgeToMirai(logger)
    MesagistoConfig.builder {
      name = "mirai"
      natsAddress = Config.nats.address
      cipherKey = Config.cipher.key
      proxyEnable = Config.proxy.enable
      proxyUri = Config.proxy.address
      resolvePhotoUrl = { uid, _ ->
        runCatching {
          val image = Image(uid.toString(charset = Charsets.UTF_8))
          image.queryUrl()
        }
      }
    }.apply()
    launch {
      Receive.recover()
    }

    listener = eventChannel.subscribeAlways(MiraiListener::handle)
    commandsListener = eventChannel.subscribeAlways(Command::handle)
    Logger.info { "Mesagisto信使已启用" }
  }

  override fun onDisable() {
    if (!Config.enable) return
    listener.complete()
    commandsListener.complete()
    Logger.info { "Mesagisto信使已禁用" }
  }
}
