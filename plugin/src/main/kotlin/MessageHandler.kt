package org.meowcat.mesagisto.mirai

import io.nats.client.Nats
import io.nats.client.impl.Headers
import io.nats.client.impl.NatsMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.GroupMessageEvent
import kotlin.coroutines.CoroutineContext

object MessageHandler : CoroutineScope {
   private val nc = Nats.connect(MesagistoConfig.address)
   private val dispatcher = nc.createDispatcher { }
   private val config = MesagistoConfig
   private val mapper = config.targetChannelMapper

   private val cid by lazy { nc.serverInfo.clientId.toString() }
   private val natsHeaders by lazy { Headers().add("cid", cid) }

   public fun handle(event: GroupMessageEvent) {
      event.handle()
   }
   @JvmName("do-handle")
   fun GroupMessageEvent.handle() {
      // 获取目标群聊的信使地址,若不存在则返回
      val channel = mapper.get(subject.id) ?: return
      // 此Bot能接收到消息说明该bot可用,加入到保有(posedo)集合
      channelPosedo.getOrPut(channel) { HashSet() }.add(group)
      // 获取目标群聊负责监听的Bot(rektoro)
      val rektoro = targetRektoro.getOrPut(subject.id) { bot }
      // 若不是负责人则返回
      if (rektoro != bot) return
      // 构建信使消息
      val content = "$senderName:${message.contentToString()}".toByteArray()
      val mesage = NatsMessage(channel, null, natsHeaders, content, false)
      // 发送信使消息
      nc.publish(mesage)
      // 监听消息
      finos.getOrPut(channel) {
         dispatcher.subscribe(channel) {
            if (it.headers["cid"].contains(cid)) return@subscribe
            launch run@{
               val group = channelPosedo.get(channel)?.random() ?: return@run
               // TODO 掉线处理
               group.sendMessage(String(it.data))
            }
         }
      }
   }

   override val coroutineContext: CoroutineContext
      get() = GlobalScope.coroutineContext
}
