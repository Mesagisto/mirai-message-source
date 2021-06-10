package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.command.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.contact.isOperator

object MesagistoCommand : CompositeCommand(
   MesagistoPlugin, "forward", "f", "fd",
   description = "当前信使插件支持的指令."
) {
   private val config = MesagistoConfig

   @SubCommand("setChannel", "channel", "sc")
   @Description("设置当前群聊的信使频道")
   suspend fun MemberCommandSender.handleSetChannel(channel: String) {
      doHandleSetChannel(channel)
   }
   @SubCommand("setChannel", "channel", "sc")
   @Description("设置当前群聊的信使频道")
   suspend fun MemberCommandSender.handleSetChannel() {
      doHandleSetChannel(null)
   }

   suspend fun MemberCommandSender.doHandleSetChannel(channel: String?) {
      if (!user.isOperator()) {
         sendMessage("您不是群主或管理员,无法设置信使频道")
         return
      }
      if (channel != null) {
         sendMessage("成功将目标群聊: ${subject.name} 的信使频道设置为 $channel")
         config.targetChannelMapper.put(subject.id, channel)
      } else {
         sendMessage("成功将目标群聊: ${subject.name} 的信使频道设置为 ${user.id}")
         config.targetChannelMapper.put(subject.id, user.id.toString())
      }
   }
}
