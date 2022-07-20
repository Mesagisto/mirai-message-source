package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
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
    if (!text.startsWith("/信使") and !text.startsWith("/f")) return
    val args = text.split(" ")
    when (args.getOrNull(1)) {
      "help", "帮助", null -> {
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
          /f ban = /信使 封禁
          /f unban = /信使 解封
          /f disable group = /信使 禁用 群组
          /f enable group = /信使 启用 群组
          /f disable channel = /信使 禁用 频道
          /f enable channel = /信使 启用 频道
        """.trimIndent()
        group.sendMessage(reply)
      }
      "bind", "绑定" -> {
        sender.bindChannel(args.getOrNull(2))
      }
      "unbind", "解绑" -> sender.unbindChannel()
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
      "disable", "禁用" -> {
        sender.disable(args.getOrNull(2))
      }
      "enable", "启用" -> {
        sender.enable(args.getOrNull(2))
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
  private suspend fun Member.about() {
    group.sendMessage("GitHub项目主页 https://github.com/MeowCat-Studio/mesagisto")
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
  private suspend fun Member.disable(type: String?) {
    when (type) {
      null -> group.sendMessage("请提供参数channel或group")
      "group", "群组" -> {
        if (Config.disablegroup.contains(group.id)) {
          group.sendMessage("此群组已经禁用过信使了")
          return
        }
        if (Config.bindings.get(group.id) == null) {
          group.sendMessage("此群组不存在信使频道，无需禁用")
          return
        }
        Config.disablegroup.add(group.id)
        group.sendMessage("已为此群组禁用信使")
      }
      "channel", "频道" -> {
        if (Config.bindings.get(group.id) == null) {
          group.sendMessage("此群组不存在信使频道，无需禁用")
          return
        }
        val channel: String = Config.bindings.get(group.id).toString()
        if (Config.disablechannel.contains(channel)) {
          group.sendMessage("已经在此频道mirai侧禁用过信使了")
          return
        }
        Config.disablechannel.add(channel)
        group.sendMessage("已为此频道mirai侧禁用信使")
      }
    }
  }
  private suspend fun Member.enable(type: String?) {
    when (type) {
      null -> group.sendMessage("请提供参数channel或group")
      "group", "群组" -> {
        if (Config.bindings.get(group.id) == null) {
          group.sendMessage("此群组不存在信使频道，无需操作")
          return
        }
        if (!Config.disablegroup.contains(group.id)) {
          group.sendMessage("此群组未禁用信使")
          return
        }
        Config.disablegroup.remove(group.id)
        group.sendMessage("已为此群组启用信使")
      }
      "channel", "频道" -> {
        if (Config.bindings.get(group.id) == null) {
          group.sendMessage("此群组不存在信使频道，无需操作")
          return
        }
        val channel: String = Config.bindings.get(group.id).toString()
        if (!Config.disablechannel.contains(channel)) {
          group.sendMessage("此频道mirai侧未禁用信使")
          return
        }
        Config.disablechannel.remove(channel)
        group.sendMessage("此频道mirai侧信使已解禁")
      }
    }
  }
}
