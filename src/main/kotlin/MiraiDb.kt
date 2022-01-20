package org.meowcat.mesagisto.mirai

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.meowcat.mesagisto.client.Logger
import org.meowcat.mesagisto.client.toByteArray
import org.rocksdb.Options
import org.rocksdb.RocksDB
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

object MiraiDb : AutoCloseable {
  private val msgSrcDbMap by lazy {
    ConcurrentHashMap<Long, RocksDB>()
  }
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  fun putMsgSource(source: MessageSource) {
    val msgSrcDb = msgSrcDbMap.getOrPut(source.targetId) {
      Logger.debug { "历史消息数据库未发现,正在创建新数据库..." }
      val options = Options()
        .setTtl(3600 * 48L)
        .setCreateIfMissing(true)
      Path("db/mirai/history").createDirectories()
      RocksDB.open(options, "db/mirai/history/${source.targetId}")
    }
    msgSrcDb.put(
      source.ids.first().toByteArray(),
      MiraiProtoBuf.encodeToByteArray(MessageSource.serializer(), source)
    )
  }
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
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

@OptIn(
  ExperimentalSerializationApi::class,
  MiraiExperimentalApi::class,
  InternalSerializationApi::class
)
private val MiraiProtoBuf = ProtoBuf {
  serializersModule = MessageSerializers.serializersModule
}
