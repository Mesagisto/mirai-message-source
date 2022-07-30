package org.meowcat.mesagisto.mirai.handlers

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.meowcat.mesagisto.client.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.data.Message
import org.meowcat.mesagisto.mirai.*

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
  // 判断多Bot下是否改对该消息作出回应
  if (!MultiBot.shouldReact(event.group, bot)) return
  // 黑名单检查
  if (Config.perm.strict && sender.id in Config.blacklist) return
  // 保存聊天记录用于引用回复
  val msgId = message.ids.first()
  MiraiDb.putMsgSource(event.source)
  // 构建消息
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
        // 拼装消息与下载图片异步进行
        Plugin.launch {
          Cache.fileByUrl(imageID, it.queryUrl()).getOrThrow()
        }
        MessageType.Image(imageID)
      }
      is FlashImage -> {
        val image = it.image
        val imageID = image.imageId.toByteArray()
        Res.storePhotoId(imageID)
        // 拼装消息与下载图片异步进行
        Plugin.launch {
          Cache.fileByUrl(imageID, image.queryUrl()).getOrThrow()
        }
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
      is Face -> {
        MessageType.Text(it.contentToString())
      }
      // 拦截MessageSource与MessageOrigin等，防止出现莫名其妙的UnsupporredMessage
      is MessageMetadata -> null // 其实现，如QuoteReply应在此处以上添加
      else -> MessageType.Text("unsupported message")
    }
  }

  val message = Message(
    profile = Profile(
      sender.id.toByteArray(),
      // 等待Mirai实现QID
      sender.id.toString(),
      // TODO Unicode空白控制符
      sender.nameCardOrNick.ifEmpty { null }
    ),
    id = msgId.toByteArray(),
    reply = replyId,
    chain
  )
  val packet = Packet.from(message.left())
  Server.send(subject.id.toString(), natsAddress, packet)
}
