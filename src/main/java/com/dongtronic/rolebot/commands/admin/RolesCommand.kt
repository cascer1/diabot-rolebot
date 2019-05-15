package com.dongtronic.rolebot.commands.admin

import com.dongtronic.rolebot.commands.RolebotCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission

/**
 * @author John Grosh (jagrosh)
 */
class RolesCommand(category: Category) : RolebotCommand(category, null) {

    init {
        this.name = "roles"
        this.help = "Get all roles in the server"
        this.guildOnly = true
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {

        val guild = event.guild
        val roles = guild.roles

        val returned = StringBuilder()

        for (role in roles) {
            returned.append("\n").append(role.id).append(" - ").append(role.name)
        }

        event.reply(returned.toString())
    }

}