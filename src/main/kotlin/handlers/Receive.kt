package org.meowcat.mesagisto.mirai.handlers

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.meowcat.mesagisto.mirai.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.mirai.MultiBot.Speakers
import org.meowcat.mesagisto.mirai.Plugin.Config
import org.mesagisto.client.*
import org.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.mesagisto.client.utils.ControlFlow
import org.mesagisto.client.utils.Either
import org.mesagisto.client.utils.right

object Receive {
  suspend fun packetHandler(pkt: Packet): Result<ControlFlow<Packet, Unit>> = withCatch(Plugin.coroutineContext) fn@{
    if (pkt.ctl != null) {
      return@fn ControlFlow.Continue(Unit)
    }
    pkt.decrypt()
      .onFailure {
        Logger.warn { "数据解密失败" }
      }
      .onSuccess {
        when (it) {
          is Either.Left -> {
            val futs = arrayListOf<Deferred<Result<Unit>>>()
            for (target in Config.targetId(pkt.roomId) ?: return@fn ControlFlow.Break(pkt)) {
              if (!it.value.from.contentEquals(target.toByteArray())) {
                futs += async {
                  msgHandler(it.value, target, "mesagisto").onFailure { e -> Logger.error(e) }
                }
              }
            }
            futs.joinAll()
          }
          is Either.Right -> {
            when (it.value) {
              is Event.RequestImage -> {
                val inbox = pkt.inbox as? Inbox.Request ?: return@fn ControlFlow.Break(pkt)
                val imageRequest = it.value as Event.RequestImage
                val image = Image(imageRequest.id.toString(charset = Charsets.UTF_8))
                val url = image.queryUrl()
                val event = Event.RespondImage(imageRequest.id, url)
                val packet = Packet.new(pkt.roomId, event.right())
                Server.respond(packet, "mesagisto", inbox.id)
              }
              else -> return@fn ControlFlow.Break(pkt)
            }
          }
        }
      }

    ControlFlow.Continue(Unit)
  }
  suspend fun recover() {
    for (roomAddress in Config.bindings.values) {
      add(roomAddress)
    }
  }
  suspend fun add(roomAddress: String) {
    val roomId = Server.roomId(roomAddress)
    Server.sub(roomId, "mesagisto")
  }
  suspend fun change(before: String, after: String) {
    del(before)
    add(after)
  }
  suspend fun del(roomAddress: String) {
    val roomId = Server.roomId(roomAddress)
    // FIXME 同侧互通 考虑当接受到不属于任何群聊的数据包时才unsub
    // TODO 更新Config中的cache
    Server.unsub(roomId, "mesagisto")
  }
}

private suspend fun msgHandler(
  message: Message,
  target: Long,
  server: String
): Result<Unit> = runCatching fn@{
  val group = Speakers[target]?.random() ?: return@fn
  if (Config.disableGroup.contains(group.id)) return@fn
  if (Config.disableChannel.contains(Config.bindings[group.id])) return@fn

  val room = Config.roomAddress(target)
  val roomId = Config.roomId(target) ?: return@fn

  val senderName = with(message.profile) { nick ?: username ?: id.toString() }
  var chain = message.chain.flatMap map@{ it ->
    when (it) {
      is MessageType.Text -> listOf(PlainText("\n${it.content}"))
      is MessageType.Image -> {
        val file = Cache.file(it.id, it.url, roomId, server).getOrThrow()
        val image = if (file.isWebp()) {
          Logger.debug { "图片为QQ不支持的WEBP格式,正在转为PNG格式..." }
          Res.convertFile(it.id) { from, to ->
            runCatching {
              convertWebpToPng(from, to)
            }
          }.onFailure { Logger.error(it) }
          file.toFile()
        } else {
          file.toFile()
        }.uploadAsImage(group)
        listOf(PlainText("\n"), image)
      }
    }
  }.toMessageChain()
  chain = PlainText("$senderName: ").plus(chain)
  run {
    val replyId = message.reply ?: return@run
    val localId = Db.getMsgIdAsI32(target, replyId) ?: return@run
    val msgSource = MiraiDb.getMsgSource(target, localId) ?: return@run
    chain += QuoteReply(msgSource)
  }
  val receipt = group.sendMessage(chain)
  Db.putMsgId(target, message.id, receipt.source.ids.first())
  MiraiDb.putMsgSource(receipt.source)
}
