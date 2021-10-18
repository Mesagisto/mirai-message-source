package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import java.util.concurrent.ConcurrentHashMap

// for listening
val Listener = ConcurrentHashMap<Long, Bot>()
// for speaking
val Speakers = ConcurrentHashMap<Long, HashSet<Group>>()
