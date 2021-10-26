package org.meowcat.mesagisto.mirai.handlers

import arrow.core.left
import io.nats.client.impl.NatsMessage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.meowcat.mesagisto.client.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.Message
import org.meowcat.mesagisto.mirai.Config
import org.meowcat.mesagisto.mirai.Listener
import org.meowcat.mesagisto.mirai.MiraiDb
import org.meowcat.mesagisto.mirai.Speakers
import kotlin.collections.HashSet

private val config = Config
private val mapper = Config.targetChannelMapper

suspend fun sendCommon(
  event: GroupMessageEvent
): Unit = with(event) {
  // 获取目标群聊的信使地址,若不存在则返回
  val channel = mapper[subject.id] ?: return
  // 此Bot能接收到消息说明该bot可用,加入到保有(posedo)集合
  Speakers.getOrPut(subject.id) { HashSet() }.add(group)
  // 获取目标群聊负责监听的Bot(rektoro)
  val rektoro = Listener.getOrPut(subject.id) { bot }
  // 若不是负责人则返回
  if (rektoro != bot) return

  MiraiDb.putMsgSource(event.source)
  // 构建消息
  val msgId = message.ids.first()
  Db.putMsgId(subject.id, msgId, msgId, false)
  var replyId: ByteArray? = null
  val chain = message.mapNotNull map@{
    when (it) {
      is PlainText -> {
        if (!it.isContentBlank()) {
          MessageType.Text(it.content)
        } else null
      }
      is Image -> {
        Res.storePhotoId(it.imageId)
        Cache.fileByUrl(it.imageId, it.queryUrl()).getOrThrow()
        MessageType.Image(it.imageId)
      }
      is QuoteReply -> {
        val localId = it.source.ids.first()
        replyId = Db.getMsgId(subject.id, localId)
        null
      }
      is At -> null
      else -> null
    }
  }

  val message = Message(
    profile = Profile(
      sender.id,
      sender.nick.ifEmpty { null },
      sender.nameCard.ifEmpty { null }
    ),
    id = msgId.toByteArray(),
    reply = replyId,
    chain
  )
  val packet = if (Config.cipher.enable) {
    Packet.encryptFrom(message.left())
  } else {
    Packet.from(message.left())
  }
  Logger.trace { "Assembling the packet" }
  Server.sendAndRegisterReceive(subject.id, channel, packet) receive@{ it, id ->
    return@receive receive(it as NatsMessage, id)
  }
}
