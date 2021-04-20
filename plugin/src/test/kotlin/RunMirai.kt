package org.meowcat.mesagisto.mirai

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@ConsoleExperimentalApi
suspend fun main() {

   MiraiConsoleTerminalLoader.startAsDaemon()

   MesagistoPlugin.load()
   MesagistoPlugin.enable()

   MiraiConsole.job.join()
}
