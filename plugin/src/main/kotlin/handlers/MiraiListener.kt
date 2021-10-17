package org.meowcat.mesagisto.mirai.handlers

import net.mamoe.mirai.event.events.GroupMessageEvent

object MiraiListener {
  suspend fun handle(event: GroupMessageEvent) {
    sendCommon(event)
  }
}
