package com.dongtronic.rolebot.listener

import com.dongtronic.rolebot.commands.RolebotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.ChannelType
import java.awt.Color
import java.util.*
import java.util.function.Consumer

class HelpListener : Consumer<CommandEvent> {
    override fun accept(event: CommandEvent) {
        if (!event.isFromType(ChannelType.TEXT)) {
            // Don't accept DM help requests, since we can't check permissions there
            event.replyError("Help command can only be used inside a server")
            return
        }

        val embedBuilder = EmbedBuilder()

        // event == com.jagrosh.jdautilities.command.CommandEvent
        val allCommands = event.client.commands

        if (event.args.isEmpty()) {
            // Show generic help card
            buildGeneralHelp(allCommands, event)
        } else {
            // Show extended help card
            buildSpecificHelp(embedBuilder, allCommands, event)
            event.reply(embedBuilder.build())
        }
    }

    private fun buildGeneralHelp(allCommands: List<Command>, event: CommandEvent) {
        val allowedCommands = filterAllowedCommands(allCommands, event)
        val categorizedCommands = groupCommands(allowedCommands)


        for (category in categorizedCommands.entries) {
            val categoryBuilder = EmbedBuilder()
            buildCategoryHelp(categoryBuilder, category)
            event.reply(categoryBuilder.build())
        }
    }

    private fun buildSpecificHelp(builder: EmbedBuilder, allCommands: List<Command>, event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val commandName = args[0]
        val subcommandName = when (args.size) {
            2 -> args[1]
            else -> ""
        }

        var found = false

        for (command in allCommands) {
            val foundCommand = getHelpCommand(command, commandName, subcommandName)
            if (foundCommand != null) {
                buildExtendedCommandHelp(builder, foundCommand)
                found = true
                break
            }
        }

        if (!found) {
            builder.setTitle("error")
            builder.setDescription("Command $commandName $subcommandName does not exist")
            builder.setColor(Color.red)
        }
    }

    private fun getHelpCommand(command: Command, commandName: String, subcommandName: String): Command? {

        if(command.isCommandFor(commandName) && subcommandName.isEmpty()) {
            return command
        }


        for (subCommand in command.children) {
            val foundCommand = getHelpCommand(subCommand, subcommandName, "")
            if (foundCommand != null) {
                return foundCommand
            }
        }

        return null
    }

    private fun buildCategoryHelp(builder: EmbedBuilder, category: Map.Entry<String, ArrayList<Command>>) {
        val categoryName = category.key
        val commands = category.value

        builder.appendDescription("**$categoryName**\n")

        for (command in commands) {
            buildCommandHelp(builder, command, null)
        }

        builder.appendDescription("\n")
    }

    /**
     * Build a basic command help line
     */
    private fun buildCommandHelp(builder: EmbedBuilder, command: Command, parentName: String?) {
        if (parentName != null) {
            builder.appendDescription(parentName)
            builder.appendDescription(" ")
        }

        builder.appendDescription(command.name)
        if (command.arguments != null) {
            builder.appendDescription(" " + command.arguments)
        }

        builder.appendDescription(" => ")
        builder.appendDescription(command.help)
        builder.appendDescription("\n")

        if (command.children.isNotEmpty()) {
            for (subcommand in command.children) {
                builder.appendDescription("   ")
                buildCommandHelp(builder, subcommand, command.name)
            }
        }
    }

    /**
     * Build extended command help card. This includes arguments, permissions, aliases, and examples
     */
    private fun buildExtendedCommandHelp(builder: EmbedBuilder, command: Command) {
        val isExtendedCommand = command is RolebotCommand
        var extendedCommand: RolebotCommand? = null
        if (isExtendedCommand) {
            extendedCommand = command as RolebotCommand
        }

        builder.setTitle("Help")
        builder.setColor(Color.magenta)
        builder.addField("Name", command.name, false)
        builder.addField("Description", command.help, false)

        if (command.arguments != null) {
            builder.addField("Arguments", command.arguments, false)
        }

        if (command.userPermissions.isNotEmpty()) {
            builder.addField("Required permissions", Arrays.toString(command.userPermissions), false)
        }

        if (command.aliases.isNotEmpty()) {
            builder.addField("Aliases", Arrays.toString(command.aliases), false)
        }

        if (command.children.isNotEmpty()) {
            builder.addField("Sub commands", Arrays.toString(command.children), false)
        }

        if (isExtendedCommand && extendedCommand!!.examples.isNotEmpty()) {
            val examples = StringBuilder()

            for (example in extendedCommand.examples) {
                examples.append("`").append(example).append("`\n")
            }

            builder.addField("Examples", examples.toString(), true)
        }
    }

    /**
     * Returns a list of only the commands a user has permission to use
     * @param commands list of all commands
     * @param event original CommandEvent. Used for checking permissions
     * @return list of commands the user is authorized to use
     */
    private fun filterAllowedCommands(commands: List<Command>, event: CommandEvent): ArrayList<Command> {
        val allowedCommands = ArrayList<Command>()

        for (command in commands) {
            if (command.isHidden) {
                continue
            }

            val requiredPermissions = command.userPermissions

            if (requiredPermissions.isEmpty()) {
                allowedCommands.add(command)
                continue
            }

            val userPermissions = event.member.permissions

            var userIsAllowedToUseCommand = true

            for (requiredPermission in requiredPermissions) {
                if (!userPermissions.contains(requiredPermission)) {
                    userIsAllowedToUseCommand = false
                }
            }

            if (userIsAllowedToUseCommand) {
                allowedCommands.add(command)
            }

        }

        return allowedCommands
    }

    /**
     * Returns a collection of commands grouped by category
     * @param commands list of commands that need to be sorted
     * @return collection of grouped and sorted commands
     */
    private fun groupCommands(commands: List<Command>): TreeMap<String, ArrayList<Command>> {
        val categorizedCommands = TreeMap<String, ArrayList<Command>>()

        for (command in commands) {
            var categoryName = "Misc"
            try {
                categoryName = command.category.name
            } catch (ex: IllegalStateException) {
                // Ignored on purpose
            }

            var categoryCommands: ArrayList<Command>? = categorizedCommands[categoryName]

            if (categoryCommands == null) {
                categoryCommands = ArrayList()
            }

            categoryCommands.add(command)

            categorizedCommands[categoryName] = categoryCommands
        }
        return categorizedCommands

    }
}
