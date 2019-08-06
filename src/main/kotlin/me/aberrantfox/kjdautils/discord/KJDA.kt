package me.aberrantfox.kjdautils.discord

import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.event.EventRegister
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.entities.User as JDAUser
import net.dv8tion.jda.api.hooks.EventListener

class KJDA(override val jda: JDA) : Discord {

  override fun addEventListener(er: EventRegister): Unit {
    jda.addEventListener(object: EventListener {
      override fun onEvent(evt: GenericEvent) {
        er.onEvent(evt)
      }
    })
  }

  override fun getUserById(userId: String): User {
    return jda.getUserById(userId)!!.fromJDA()
  }

  companion object {
    fun build(config: KConfiguration) =
            KJDA(JDABuilder(AccountType.BOT).setToken(config.token).build())
  }
}

fun JDAUser.fromJDA(): User { return KJDAUser(this) }

class KJDAUser(private val jdaUser: JDAUser) : User {
  override val isBot = jdaUser.isBot

  override fun sendPrivateMessage(msg: String, log: BotLogger) {
    jdaUser.sendPrivateMessage(msg, log)
  }

  override fun sendPrivateMessage(msg: MessageEmbed, log: BotLogger) {
    jdaUser.sendPrivateMessage(msg, log)
  }
}
