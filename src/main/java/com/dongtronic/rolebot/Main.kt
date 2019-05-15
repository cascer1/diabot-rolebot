package com.dongtronic.rolebot

import com.dongtronic.rolebot.commands.admin.AdminCommand
import com.dongtronic.rolebot.commands.admin.EnforceRolesCommand
import com.dongtronic.rolebot.commands.admin.RolesCommand
import com.dongtronic.rolebot.commands.admin.ShutdownCommand
import com.dongtronic.rolebot.commands.misc.*
import com.dongtronic.rolebot.listener.*
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.AboutCommand
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import org.slf4j.LoggerFactory
import javax.security.auth.login.LoginException

object Main {

    private val logger = LoggerFactory.getLogger(Main::class.java)

    @Throws(LoginException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val token = System.getenv("ROLEBOTTOKEN")

        // create command categories
        val adminCategory = Category("Admin")
        val utilitiesCategory = Category("Utilities")

        // define an eventwaiter, dont forget to add this to the JDABuilder!
        val waiter = EventWaiter()

        // define a command client
        val client = CommandClientBuilder()

        // The default is "Type !!help" (or whatver prefix you set)
        client.useDefaultGame()

        client.setStatus(OnlineStatus.INVISIBLE)

        // sets emojis used throughout the bot on successes, warnings, and failures
        client.setEmojis("\uD83D\uDC4C", "\uD83D\uDE2E", "\uD83D\uDE22")


        // sets the bot prefix
        if (System.getenv("ROLEBOT_DEBUG") != null) {
            client.setPrefix("rl ")
        } else {
            client.setPrefix("rolebot ")
        }

        client.setOwnerId("189436077793083392")

        // adds commands
        client.addCommands(
                // command to show information about the bot
                AboutCommand(java.awt.Color(0, 0, 255), "a role enforcement bot",
                        arrayOf("Exclusive role enforcement"),
                        Permission.ADMINISTRATOR),


                // Utility
                PingCommand(utilitiesCategory),

                // Admin
                AdminCommand(adminCategory),
                RolesCommand(adminCategory),
                EnforceRolesCommand(adminCategory),
                ShutdownCommand(adminCategory)
        )


        // Custom help handler
        client.setHelpConsumer(HelpListener())


        // start getting a bot account set up
        JDABuilder(AccountType.BOT)
                // set the token
                .setToken(token)

                // set the game for when the bot is loading
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setGame(Game.playing("loading..."))

                // add the listeners
                .addEventListener(waiter)
                .addEventListener(client.build())

                // start it up!
                .build()


    }

}
