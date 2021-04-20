package org.meowcat.mesagisto.mirai

import io.nats.client.Connection
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import java.util.concurrent.ConcurrentHashMap

val wsKeeper = ConcurrentHashMap<String, Connection>()
val addressEntity = ConcurrentHashMap<String, HashSet<Group>>()

val groupHandler = ConcurrentHashMap<Long, Bot>()
val groupBots = ConcurrentHashMap<Long, HashSet<Bot>>()
