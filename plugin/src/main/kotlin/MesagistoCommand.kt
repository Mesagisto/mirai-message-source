package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.command.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.contact.isOperator

object MesagistoCommand : CompositeCommand(
   MesagistoPlugin, "forward", "f", "fd",
   description = "Commands for Itsusinn's forward plugin."
) {

   @SubCommand("setChannel", "channel", "sc")
   suspend fun MemberCommandSender.handleSetTarget() {
      if (!user.isOperator()) {
         sendMessage("您不是群主或管理员,无法设置信使频道")
         return
      }

      sendMessage("set target to ${subject.id}")
   }
}
