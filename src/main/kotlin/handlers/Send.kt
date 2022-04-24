package org.meowcat.mesagisto.mirai.handlers

import io.nats.client.impl.NatsMessage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.meowcat.mesagisto.client.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.Message
import org.meowcat.mesagisto.mirai.*
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
        // 有时mirai会出现没有内容的消息,过滤
        if (!it.isContentBlank()) {
          MessageType.Text(it.content)
        } else null
      }
      is Image -> {
        Res.storePhotoId(it.imageId.toByteArray())
        Cache.fileByUrl(it.imageId.toByteArray(), it.queryUrl()).getOrThrow()
        MessageType.Image(it.imageId.toByteArray())
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
      sender.id.toByteArray(),
      // 等待Mirai实现QID
      sender.id.toString(),
      sender.nameCardOrNick.ifEmpty { null }
    ),
    id = msgId.toByteArray(),
    reply = replyId,
    chain
  )
  val packet = Packet.from(message.left())
  Server.send(subject.id.toString(), natsAddress, packet)
}
