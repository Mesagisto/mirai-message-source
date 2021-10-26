package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.utils.MiraiLogger
import org.meowcat.mesagisto.client.ILogger
import org.meowcat.mesagisto.client.LogLevel
import org.meowcat.mesagisto.client.Logger

fun Logger.bridgeToMirai(impl: MiraiLogger) {
  level = when (impl) {
//    Level.ALL -> LogLevel.TRACE
//    Level.FINE -> LogLevel.TRACE
//    Level.INFO -> LogLevel.TRACE
//    Level.WARNING -> LogLevel.WARN
//    Level.SEVERE -> LogLevel.ERROR
//    Level.OFF -> LogLevel.ERROR
    else -> { LogLevel.TRACE }
  }
  provider = object : ILogger {
    override fun log(level: LogLevel, msg: String) {
      when (level) {
//        LogLevel.TRACE -> impl.fine(msg)
//        LogLevel.DEBUG -> impl.finer(msg)
        LogLevel.INFO -> impl.info(msg)
        else -> impl.info(msg)
      }
    }
  }
}
