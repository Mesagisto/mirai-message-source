package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.meowcat.mesagisto.mirai.handlers.Receive
import kotlin.text.toLong

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
    if (!text.startsWith("/信使") and !text.startsWith("/msgist") and !text.startsWith("/f")) return
    val args = text.split(" ")
    when (args.getOrNull(1)) {
      "help", "帮助", null -> sender.help()
      "bind", "绑定" -> sender.bind(args.getOrNull(2))
      "unbind", "解绑" -> sender.unbind()
      "about", "关于" -> sender.about()
      "status", "状态" -> sender.status()
      "ban", "封禁" -> {
        try {
          sender.ban(args.get(2).toLong())
        } catch (e: NumberFormatException) {
          group.sendMessage("参数不合法，请提供一个QQ号")
        } catch (e: IndexOutOfBoundsException) {
          group.sendMessage("缺少参数，请提供一个QQ号")
        }
      }
      "unban", "解封" -> {
        try {
          sender.unban(args.get(2).toLong())
        } catch (e: NumberFormatException) {
          group.sendMessage("参数不合法，请提供一个QQ号")
        } catch (e: IndexOutOfBoundsException) {
          group.sendMessage("缺少参数，请提供一个QQ号")
        }
      }
    }
  }

  private suspend fun Member.bind(channel: String?) {
    val address = channel ?: group.id.toString()
    if (Config.bindings.put(group.id, address) != null) {
      Receive.change(group.id, address)
      group.sendMessage("成功将群聊: ${group.name} 的信使频道变更为$address")
    } else {
      Receive.add(group.id, address)
      group.sendMessage("成功将群聊: ${group.name} 的信使频道设置为$address")
    }
  }
  private suspend fun Member.unbind() {
    Config.bindings.remove(group.id)
    Receive.del(group.id)
    group.sendMessage("已解绑本群的信使频道")
  }
  private suspend fun Member.about() {
    group.sendMessage("GitHub项目主页 https://github.com/MeowCat-Studio/mesagisto")
  }
  private suspend fun Member.help() {
    group.sendMessage(
      """
      ------  用法  ------
      /信使 绑定 [频道名]
      或 
      /msgist bind [频道名]
      例如
      /msgist bind 114514、/信使 绑定 114514 等
      ------  列表  ------
      /msgist help = /信使 帮助
      /msgist bind = /信使 绑定
      /msgist unbind = /信使 解绑
      /msgist about = /信使 关于
      /msgist status = /信使 状态
      """.trimIndent()
    )
  }
  private suspend fun Member.status() {
    group.sendMessage("唔... 也许是在正常运行?")
  }
  private suspend fun Member.ban(id: Long) {
    if (Config.blacklist.contains(id)) {
      group.sendMessage("$id 已经被封禁了")
    } else {
      Config.blacklist.add(id)
      group.sendMessage("已成功封禁：$id")
    }
  }
  private suspend fun Member.unban(id: Long) {
    if (Config.blacklist.contains(id)) {
      Config.blacklist.remove(id)
      group.sendMessage("已成功解封：$id")
    } else {
      group.sendMessage("$id 没有被封禁")
    }
  }
}
