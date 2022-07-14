package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import java.util.concurrent.ConcurrentHashMap

object MultiBot {
  // for listening
  var Listeners = ConcurrentHashMap<Long, Bot>()

  // for speaking
  val Speakers = ConcurrentHashMap<Long, HashSet<Group>>()
  fun handleBotOnline(event: BotOnlineEvent) {
    addBot(event.bot)
  }
  fun handleBotJoinGroup(event: BotJoinGroupEvent) {
    addBot(event.bot)
  }
  fun handleBotLeaveGroup(event: BotLeaveEvent) {
    delBot(event.bot)
  }
  private fun addBot(bot: Bot) {
    delBot(bot)
    bot.groups.forEach {
      Listeners[it.id] = bot
      Speakers.getOrPut(it.id) { HashSet() }.add(it)
    }
  }
  private fun delBot(bot: Bot) {
    Listeners.filterTo(Listeners) {
      it.value.id != bot.id
    }
    Speakers.forEachValue(4) { groups ->
      groups.filterTo(groups) { group ->
        group.bot.id != bot.id
      }
    }
  }
}
