package org.meowcat.mesagisto.mirai // ktlint-disable filename

import net.mamoe.mirai.utils.MiraiLogger
import org.meowcat.mesagisto.client.ILogger
import org.meowcat.mesagisto.client.LogLevel
import org.meowcat.mesagisto.client.Logger
import java.util.logging.Level

fun Logger.bridgeToMirai(impl: MiraiLogger) {
  level = when (impl) {
    Level.ALL -> LogLevel.TRACE
    Level.FINE -> LogLevel.TRACE
    Level.INFO -> LogLevel.TRACE
    Level.WARNING -> LogLevel.WARN
    Level.SEVERE -> LogLevel.ERROR
    Level.OFF -> LogLevel.ERROR
    else -> { LogLevel.TRACE }
  }
  provider = object : ILogger {
    override fun log(level: LogLevel, msg: String) {
      when (level) {
        LogLevel.TRACE -> impl.info(msg)
        LogLevel.DEBUG -> impl.info(msg)
        LogLevel.INFO -> impl.info(msg)
        LogLevel.WARN -> impl.warning(msg)
        LogLevel.ERROR -> impl.error(msg)
        else -> impl.info(msg)
      }
    }
  }
}
