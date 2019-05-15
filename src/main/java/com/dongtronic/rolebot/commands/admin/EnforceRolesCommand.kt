package com.dongtronic.rolebot.commands.admin

import com.dongtronic.rolebot.commands.RolebotCommand
import com.dongtronic.rolebot.util.ExclusiveRoles
import com.dongtronic.rolebot.util.NicknameUtils
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory

class EnforceRolesCommand(category: Category) : RolebotCommand(category, null) {

    init {
        this.name = "enforce"
        this.help = "Enforces roles"
        this.guildOnly = false
        this.ownerCommand = false
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.hidden = false
    }

    override fun execute(event: CommandEvent) {
        val guild = event.guild

        logger.info("Starting role enforcement for ${guild.id} (requested by ${NicknameUtils.determineAuthorDisplayName(event)})")

        guild.members.forEach { member ->
            ExclusiveRoles.exclusiveRoles.forEach { pair ->
                if (hasRole(member, pair[0]) && hasRole(member, pair[1])) {
                    event.replyError("User *${member.effectiveName}* (`${member.user.id}`) has roles `${getRoleName(event, pair[0])}` and `${getRoleName(event, pair[1])}`")
                }
            }
        }
    }

    private fun hasRole(user: net.dv8tion.jda.core.entities.Member, roleId: String): Boolean {
        user.roles.forEach { role ->
            if (role.id == roleId) {
                return true
            }
        }
        return false
    }

    private fun getRoleName(event: CommandEvent, roleId: String): String {
        val role = event.guild.getRoleById(roleId)

        return role.name
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EnforceRolesCommand::class.java)
    }

}
