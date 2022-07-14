package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import org.meowcat.mesagisto.mirai.handlers.Receive

object Command {
  suspend fun handle(event: GroupMessageEvent): Unit = event.run {
    // 如果事件取消, 则跳出处理流程
    if (event.isCancelled) return@run
    // 如果严格模式开启且发送者不在名单内, 则跳出处理流程
    if (Config.perm.strict) {
      if (!Config.perm.users.contains(event.sender.id)) return@run
    }
    // 如果不是群主或管理员, 则跳出处理流程
    if (!event.sender.isOperator()) return@run

    val text = event.message.contentToString()
    if (!text.startsWith("/信使") and !text.startsWith("/f")) return
    val args = text.split(" ")
    when (args.getOrNull(1)) {
      "bind", "绑定" -> {
        sender.bindChannel(args.getOrNull(2))
      }
      "unbind", "解绑" -> {
        sender.unbindChannel()
      }
      "help", "帮助" -> {
        val reply = message.quote() + """
          未知指令
          ------  用法  ------
          /信使 绑定 [频道名]
          或 
          /f bind [频道名]
          例如
          /f bind 114514、/信使 绑定 114514 等
          ------  列表  ------
          /f help = /信使 帮助
          /f bind = /信使 绑定
          /f unbind = /信使 解绑
          /f about = /信使 关于
          /f status = /信使 状态
        """.trimIndent()
        group.sendMessage(reply)
      }
    }
  }

  private suspend fun Member.bindChannel(channel: String?) {
    val address = channel ?: group.id.toString()
    if (Config.bindings.put(group.id, address) != null) {
      Receive.change(group.id, address)
      group.sendMessage("成功将群聊: ${group.name} 的信使频道变更为$address")
    } else {
      Receive.add(group.id, address)
      group.sendMessage("成功将群聊: ${group.name} 的信使频道设置为$address")
    }
  }
  private suspend fun Member.unbindChannel() {
    Config.bindings.remove(group.id)
    Receive.del(group.id)
    group.sendMessage("已解绑本群的信使频道")
  }
}
