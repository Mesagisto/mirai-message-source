package org.meowcat.mesagisto.mirai.handlers

import arrow.core.Either
import io.nats.client.impl.NatsMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.meowcat.mesagisto.client.Cache
import org.meowcat.mesagisto.client.Db
import org.meowcat.mesagisto.client.Logger
import org.meowcat.mesagisto.client.Res
import org.meowcat.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.mirai.* // ktlint-disable no-wildcard-imports

// fixme change func name
suspend fun receive(
  message: NatsMessage,
  target: Long
): Result<Unit> = runCatching run@{
  Logger.trace { "Parsing packet" }
  when (val packet = Packet.fromCbor(message.data).getOrThrow()) {
    is Either.Left -> {
      receiveMessage(packet.value, target).getOrThrow()
    }
    is Either.Right -> {
      packet.value
    }
  }
}

suspend fun receiveMessage(
  message: Message,
  target: Long
): Result<Unit> = runCatching fn@{
  Logger.trace { "transferring" }
  val group = Speakers[target]?.random() ?: return@fn
  val senderName = with(message.profile) { nick ?: username ?: id.toString() }
  var chain = message.chain.flatMap map@{ it ->
    when (it) {
      is MessageType.Text -> listOf(PlainText("$senderName : ${it.content}"))
      is MessageType.Image -> {
        val file = Cache.file(it.id, it.url, Config.mapper(group)!!).getOrThrow()
        Logger.trace { "gotten file" }
        val image = if (file.isWebp()) {
          Logger.trace { "image is webp,which is not supported by qq,being converted into png" }
          Res.convertFile(it.id) { from, to ->
            runCatching {
              convertWebpToPng(from, to)
            }
          }.onFailure { Logger.error(it) }
          file.toFile()
        } else {
          file.toFile()
        }.uploadAsImage(group)
        listOf(PlainText("$senderName:"), image)
      }
    }
  }.toMessageChain()
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
