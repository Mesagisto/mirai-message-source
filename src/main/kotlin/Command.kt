package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import org.meowcat.mesagisto.mirai.handlers.Receive

object Command {
  suspend fun handle(event: GroupMessageEvent): Unit = event.run {
    val text = event.message.contentToString()
    if (!text.startsWith("/信使") and !text.startsWith("/f")) return
    val args = text.split(" ")
    when (args.getOrNull(1)) {
      "设置频道", "sc" -> {
        sender.setChannel(args.getOrNull(2))
      }
      "del", "删除" -> {
        sender.delChannel()
      }
      "help", "帮助" -> {
        val reply = message.quote() + """
          未知指令
          ------  用法  ------
          /信使 设置频道 [频道名]
          或 
          /f sc [频道名]
          频道名留空时将发送者ID作为频道名
          例如
          /f sc 114514、/信使 设置频道 等
          ------  list  ------
          /f help = /信使 帮助
          /f del = /信使 删除
          /f about
          /f status
        """.trimIndent()
        group.sendMessage(reply)
      }
    }
  }

  private suspend fun Member.setChannel(channel: String?) {
    if (!isOperator()) {
      group.sendMessage("您不是群主或管理员,无法设置信使频道")
      return
    }

    val address = channel ?: group.id.toString()
    if (Config.bindings.put(group.id, address) != null) {
      Receive.change(group.id, address)
      group.sendMessage("成功将目标群聊: ${group.name} 的信使频道变更为$address")
    } else {
      Receive.add(group.id, address)
      group.sendMessage("成功将目标群聊: ${group.name} 的信使频道设置为$address")
    }
  }
  private suspend fun Member.delChannel() {
    if (!isOperator()) {
      group.sendMessage("您不是群主或管理员,无法设置信使频道")
      return
    }
    Config.bindings.remove(group.id)
    Receive.del(group.id)
    group.sendMessage("已删除本群的信使频道")
  }
}
