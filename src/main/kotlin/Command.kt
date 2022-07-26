package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.isOperator
import org.meowcat.mesagisto.mirai.handlers.Receive

object Command : CompositeCommand(
  Plugin,
  primaryName = "msgist",
  secondaryNames = arrayOf("f", "信使"),
  description = "信使"
) {

  @SubCommand("bind", "绑定")
  suspend fun MemberCommandSender.bind(channel: String) {
    if (!user.isOperator() || !MultiBot.shouldReact(group, bot)) return

    if (Config.bindings.put(group.id, channel) != null) {
      Receive.change(group.id, channel)
      group.sendMessage("成功将群聊: ${group.name} 的信使频道变更为$channel")
    } else {
      Receive.add(group.id, channel)
      group.sendMessage("成功将群聊: ${group.name} 的信使频道设置为$channel")
    }
  }

  @SubCommand("unbind", "解绑")
  suspend fun MemberCommandSender.unbind(address: String) {
    if (!user.isOperator() || !MultiBot.shouldReact(group, bot)) return

    Config.bindings.remove(group.id)
    Receive.del(group.id)
    group.sendMessage("已解绑 ${group.name} 的信使频道")
  }

  @SubCommand("ban", "封禁")
  suspend fun MemberCommandSender.ban(user: User) {
    if (!MultiBot.shouldReact(group, bot)) return
    if (Config.perm.strict || !Config.perm.users.contains(user.id)) {
      group.sendMessage("信使的严格模式未启用 或 您不是本Mirai信使Bot的管理员")
      return
    }
    if (Config.blacklist.contains(user.id)) {
      group.sendMessage("${user.nick}-${user.id} 已经被封禁了")
    } else {
      Config.blacklist.add(user.id)
      group.sendMessage("已成功封禁：user.id")
    }
  }

  @SubCommand("unban", "解封")
  suspend fun MemberCommandSender.unban(user: User) {
    if (!MultiBot.shouldReact(group, bot)) return
    if (Config.perm.strict || !Config.perm.users.contains(user.id)) {
      group.sendMessage("信使的严格模式未启用 或 您不是本Mirai信使Bot的管理员")
      return
    }
    if (Config.blacklist.contains(user.id)) {
      Config.blacklist.remove(user.id)
      group.sendMessage("已成功解封：${user.nick}-${user.id}")
    } else {
      group.sendMessage("${user.nick}-${user.id} 没有被封禁")
    }
  }

  @SubCommand("status", "状态")
  suspend fun MemberCommandSender.status() {
    group.sendMessage("唔... 也许是在正常运行?")
  }

  @SubCommand("about", "关于")
  suspend fun MemberCommandSender.about() {
    if (!MultiBot.shouldReact(group, bot)) return
    group.sendMessage("GitHub项目主页 https://github.com/MeowCat-Studio/mesagisto")
  }

  @OptIn(ConsoleExperimentalApi::class)
  @SubCommand("disable", "禁用")
  suspend fun MemberCommandSender.disable(@Name("group/channel") type: String) {
    if (!MultiBot.shouldReact(group, bot)) return
    if (Config.perm.strict || !Config.perm.users.contains(user.id)) {
      group.sendMessage("信使的严格模式未启用 或 您不是本Mirai信使Bot的管理员")
      return
    }
    when (type) {
      "group", "群组" -> {
        if (Config.disableGroup.contains(group.id)) {
          group.sendMessage("此群组已经禁用过信使了")
          return
        }
        if (Config.bindings[group.id] == null) {
          group.sendMessage("此群组不存在信使频道，无需禁用")
          return
        }
        Config.disableGroup.add(group.id)
        group.sendMessage("已为此群组禁用信使")
      }
      "channel", "频道" -> {
        if (Config.bindings[group.id] == null) {
          group.sendMessage("此群组不存在信使频道，无需禁用")
          return
        }
        val channel: String = Config.bindings[group.id].toString()
        if (Config.disableChannel.contains(channel)) {
          group.sendMessage("已经在此频道mirai侧禁用过信使了")
          return
        }
        Config.disableChannel.add(channel)
        group.sendMessage("已为此频道mirai侧禁用信使")
      }
      else -> {
        // TODO: 2022/7/26
      }
    }
  }

  @OptIn(ConsoleExperimentalApi::class)
  @SubCommand("enable", "启用")
  suspend fun MemberCommandSender.enable(@Name("group/channel") type: String) {
    if (!MultiBot.shouldReact(group, bot)) return
    if (Config.perm.strict || !Config.perm.users.contains(user.id)) {
      group.sendMessage("信使的严格模式未启用 或 您不是本Mirai信使Bot的管理员")
      return
    }
    when (type) {
      "group", "群组" -> {
        if (Config.bindings[group.id] == null) {
          group.sendMessage("此群组不存在信使频道，无需操作")
          return
        }
        if (!Config.disableGroup.contains(group.id)) {
          group.sendMessage("此群组未禁用信使")
          return
        }
        Config.disableGroup.remove(group.id)
        group.sendMessage("已为此群组启用信使")
      }
      "channel", "频道" -> {
        if (Config.bindings[group.id] == null) {
          group.sendMessage("此群组不存在信使频道，无需操作")
          return
        }
        val channel: String = Config.bindings[group.id].toString()
        if (!Config.disableChannel.contains(channel)) {
          group.sendMessage("此频道Mirai侧未禁用信使")
          return
        }
        Config.disableChannel.remove(channel)
        group.sendMessage("此频道Mirai侧信使已解禁")
      }
      else -> {
        // TODO: 2022/7/26
      }
    }
  }
}
