package com.dongtronic.rolebot.commands.admin

import com.dongtronic.rolebot.commands.RolebotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory

class AdminCommand(category: Command.Category) : RolebotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(AdminCommand::class.java)

    init {
        this.name = "admin"
        this.help = "Administrator commands"
        this.guildOnly = true
        this.aliases = arrayOf("a")
        this.examples = arrayOf()
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.children = arrayOf()
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            event.replyError("Please specify a command")
            return
        }

        event.replyError("Unknown command: ${args[0]}")

    }




}
