package org.meowcat.mesagisto.mirai

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.message.data.* // ktlint-disable no-wildcard-imports
import org.meowcat.mesagisto.client.ensureDirectories
import org.meowcat.mesagisto.client.toByteArray
import org.rocksdb.Options
import org.rocksdb.RocksDB
import org.tinylog.kotlin.Logger
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path

object MiraiDb : AutoCloseable {
  private val msgSrcDbMap by lazy {
    ConcurrentHashMap<Long, RocksDB>()
  }
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  fun putMsgSource(source: MessageSource) {
    val msgSrcDb = msgSrcDbMap.getOrPut(source.targetId) {
      Logger.trace { "Message history db not found,creating a new one" }
      val options = Options()
        .setTtl(3600 * 48)
        .setCreateIfMissing(true)
      Path("db/mirai/history").ensureDirectories()
      RocksDB.open(options, "db/mirai/history/${source.targetId}")
    }
    msgSrcDb.put(
      source.ids.first().toByteArray(),
      ProtoBufMirai.encodeToByteArray(MessageSource.Serializer, source)
    )
  }
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  fun getMsgSource(target: Long, id: Int): MessageSource? {
    val msgSrcDb = msgSrcDbMap[target] ?: return null
    val bytes = msgSrcDb.get(id.toByteArray()) ?: return null
    return ProtoBufMirai.decodeFromByteArray(MessageSource.Serializer, bytes)
  }
  override fun close() {
    msgSrcDbMap.forEachValue(1) {
      it.close()
    }
  }
}

@OptIn(ExperimentalSerializationApi::class, net.mamoe.mirai.utils.MiraiExperimentalApi::class)
val ProtoBufMirai = ProtoBuf {
  serializersModule = SerializersModule {
    contextual(MessageChain::class, MessageChain.Serializer)
    contextual(MessageOriginKind::class, MessageOriginKind.serializer())
    fun PolymorphicModuleBuilder<MessageMetadata>.messageMetadataSubclasses() {
      subclass(MessageSource::class, MessageSource.serializer())
      subclass(QuoteReply::class, QuoteReply.serializer())
      subclass(MessageOrigin::class, MessageOrigin.serializer())
    }
    fun PolymorphicModuleBuilder<MessageContent>.messageContentSubclasses() {
      subclass(At::class, At.serializer())
      subclass(AtAll::class, AtAll.serializer())
      subclass(Face::class, Face.serializer())
      subclass(Image::class, Image.Serializer)
      subclass(PlainText::class, PlainText.serializer())
      subclass(ForwardMessage::class, ForwardMessage.serializer())
      subclass(LightApp::class, LightApp.serializer())
      subclass(SimpleServiceMessage::class, SimpleServiceMessage.serializer())
      @Suppress("DEPRECATION")
      subclass(Voice::class, Voice.serializer())
      subclass(PokeMessage::class, PokeMessage.serializer())
      subclass(VipFace::class, VipFace.serializer())
      subclass(FlashImage::class, FlashImage.serializer())
      subclass(MusicShare::class, MusicShare.serializer())
      subclass(Dice::class, Dice.serializer())
    }
    polymorphic(SingleMessage::class) {
      messageContentSubclasses()
      messageMetadataSubclasses()
    }
    polymorphic(MessageContent::class) {
      messageContentSubclasses()
    }
    polymorphic(MessageMetadata::class) {
      messageMetadataSubclasses()
    }
    polymorphic(RichMessage::class) {
      subclass(SimpleServiceMessage::class, SimpleServiceMessage.serializer())
      subclass(LightApp::class, LightApp.serializer())
    }
    polymorphic(ServiceMessage::class) {
      subclass(SimpleServiceMessage::class, SimpleServiceMessage.serializer())
    }
  }
}
