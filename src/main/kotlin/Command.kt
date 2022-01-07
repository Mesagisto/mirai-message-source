package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote

object Command {
  suspend fun handle(event: GroupMessageEvent): Unit = event.run {
    val text = event.message.contentToString()
    if (!text.startsWith("/信使") and !text.startsWith("/f")) return
    val args = text.split(" ")
    when (args.getOrNull(1)) {
      "设置频道" -> {
        sender.setChannel(args.getOrNull(2))
      }
      "sc" -> {
        sender.setChannel(args.getOrNull(2))
      }
      else -> {
        val reply = message.quote() + """
          未知指令
          ------  用法  ------
          /信使 设置频道 [频道名]
          或 
          /f sc [频道名]
          频道名留空时将发送者ID作为频道名
          例如
          /f sc 114514、/信使 设置频道 等
        """.trimIndent()
        group.sendMessage(reply)
      }
    }
  }
  private val config = Config

  private suspend fun Member.setChannel(channel: String?) {
    if (!isOperator()) {
      group.sendMessage("您不是群主或管理员,无法设置信使频道")
      return
    }
    if (channel != null) {
      group.sendMessage("成功将目标群聊: ${group.name} 的信使频道设置为 $channel")
      config.targetChannelMapper[group.id] = channel
    } else {
      group.sendMessage("成功将目标群聊: ${group.name} 的信使频道设置为 ${group.id}")
      config.targetChannelMapper[group.id] = group.id.toString()
    }
  }
}
