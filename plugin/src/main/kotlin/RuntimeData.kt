package org.meowcat.mesagisto.mirai

import io.nats.client.Subscription
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import java.util.concurrent.ConcurrentHashMap

// end points
val finos = ConcurrentHashMap<String, Subscription>()
// for listening
val targetRektoro = ConcurrentHashMap<Long, Bot>()
// for speaking
val channelPosedo = ConcurrentHashMap<String, HashSet<Group>>()
