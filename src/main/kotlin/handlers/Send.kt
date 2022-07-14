package org.meowcat.mesagisto.mirai.handlers

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.meowcat.mesagisto.client.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.Message
import org.meowcat.mesagisto.mirai.*
import org.meowcat.mesagisto.mirai.MultiBot.Listeners

object MiraiListener {
  suspend fun handle(event: GroupMessageEvent) {
    sendHandler(event)
  }
}

suspend fun sendHandler(
  event: GroupMessageEvent
): Unit = with(event) {
  // 获取目标群聊的信使地址,若不存在则返回
  val natsAddress = Config.bindings[subject.id] ?: return
  // 获取目标群聊负责监听的Bot
  val listener = Listeners.getOrPut(subject.id) { bot }
  // 若不是负责人则返回
  if (listener != bot) return
  // 若在黑名单则返回
  if (sender.id in Config.blacklist) return
  // 保存聊天记录用于引用回复
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
        val imageID = it.imageId.toByteArray()
        Res.storePhotoId(imageID)
        Cache.fileByUrl(imageID, it.queryUrl()).getOrThrow()
        MessageType.Image(imageID)
      }
      is FlashImage -> {
        val image = it.image
        val imageID = image.imageId.toByteArray()
        Res.storePhotoId(imageID)
        Cache.fileByUrl(imageID, image.queryUrl()).getOrThrow()
        MessageType.Image(imageID)
      }
      is QuoteReply -> {
        val localId = it.source.ids.first()
        replyId = Db.getMsgId(subject.id, localId)
        null
      }
      is At -> {
        MessageType.Text(it.contentToString())
      }
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
