package org.meowcat.mesagisto.mirai

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import org.fusesource.leveldbjni.JniDBFactory.factory
import org.iq80.leveldb.DB
import org.iq80.leveldb.Options
import org.meowcat.mesagisto.client.Logger
import org.meowcat.mesagisto.client.toByteArray
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

object MiraiDb : AutoCloseable {
  private val msgSrcDbMap by lazy {
    ConcurrentHashMap<Long, DB>()
  }

  @OptIn(ExperimentalSerializationApi::class)
  fun putMsgSource(source: MessageSource) {
    val msgSrcDb = msgSrcDbMap.getOrPut(source.targetId) {
      Logger.debug { "历史消息数据库未发现,正在创建新数据库..." }
      val options = Options().createIfMissing(true)
      Path("db_v2/mirai/history").createDirectories()
      factory.open(File("db_v2/mirai/history/${source.targetId}"), options)
    }
    msgSrcDb.put(
      source.ids.first().toByteArray(),
      MiraiProtoBuf.encodeToByteArray(MessageSource.serializer(), source)
    )
  }

  @OptIn(ExperimentalSerializationApi::class)
  fun getMsgSource(target: Long, id: Int): MessageSource? {
    val msgSrcDb = msgSrcDbMap[target] ?: return null
    val bytes = msgSrcDb.get(id.toByteArray()) ?: return null
    return MiraiProtoBuf.decodeFromByteArray(MessageSource.serializer(), bytes)
  }
  override fun close() {
    msgSrcDbMap.forEachValue(1) {
      it.close()
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
private val MiraiProtoBuf = ProtoBuf {
  serializersModule = MessageSerializers.serializersModule
}
