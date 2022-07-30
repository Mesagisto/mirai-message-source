package org.meowcat.mesagisto.mirai.handlers

import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.meowcat.mesagisto.client.*
import org.meowcat.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.mirai.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.mirai.MultiBot.Speakers

typealias NatsMessage = io.nats.client.Message

object Receive {
  suspend fun recover() {
    Config.bindings.forEach {
      runCatching {
        Server.recv(it.key.toString(), it.value, ::mainHandler)
      }
    }
  }
  suspend fun add(target: Long, address: String) {
    Server.recv(target.toString(), address) handler@{ msg, target_id ->
      return@handler mainHandler(msg as NatsMessage, target_id)
    }
  }
  suspend fun change(target: Long, address: String) {
    Server.unsub(target.toString())
    add(target, address)
  }
  suspend fun del(target: Long) {
    Server.unsub(target.toString())
  }
}

suspend fun mainHandler(
  message: NatsMessage,
  target: String
): Result<Unit> = runCatching run@{
  when (val packet = Packet.fromCbor(message.data).getOrThrow()) {
    is Either.Left -> {
      leftSubHandler(packet.value, target.toLong()).getOrThrow()
    }
    is Either.Right -> {
      packet.value
    }
  }
}

private suspend fun leftSubHandler(
  message: Message,
  target: Long
): Result<Unit> = runCatching fn@{
  val group = Speakers[target]?.random() ?: return@fn
  if (Config.disableGroup.contains(group.id)) return@fn
  if (Config.disableChannel.contains(Config.bindings[group.id])) return@fn
  val senderName = with(message.profile) { nick ?: username ?: id.toString() }
  var chain = message.chain.flatMap map@{ it ->
    when (it) {
      is MessageType.Text -> listOf(PlainText("\n${it.content}"))
      is MessageType.Image -> {
        val file = Cache.file(it.id, it.url, Config.mapper(group)!!).getOrThrow()
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
