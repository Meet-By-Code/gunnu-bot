package io.babydevelopers.babybot.application.spring.discord.listener

import io.babydevelopers.babybot.application.spring.discord.model.StudySlashCommand
import io.babydevelopers.babybot.application.spring.discord.model.StudySlashCommand.ADMISSION
import io.babydevelopers.babybot.application.spring.discord.model.StudySlashCommand.DELETE
import io.babydevelopers.babybot.application.spring.discord.model.StudySlashCommand.ENTER
import net.dv8tion.jda.api.Permission.ADMINISTRATOR
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Controller

private fun SlashCommandInteractionEvent.sendMessage(message: String) = reply(message).queue()
private val SlashCommandInteractionEvent._member: Member
    get() = member ?: error("멤버가 존재하지 않습니다.")
private val SlashCommandInteractionEvent.hasAdminRole: Boolean
    get() = _member.hasPermission(ADMINISTRATOR)

@Controller
class CommandStudyController(
    private val manualForumController: ManualForumController,
) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (StudySlashCommand.from(event.name)) {
            ADMISSION -> runWithAdminPrivileges(event) {
                createChannel(event)
            }

            DELETE -> runWithAdminPrivileges(event) {
                manualForumController.onChannelDelete(event)
                event.sendMessage("보이스 채널을 삭제하였습니다.")
            }

            ENTER -> enterChannel(event)
        }
    }

    private fun runWithAdminPrivileges(event: SlashCommandInteractionEvent, action: () -> Unit) {
        if (!event.hasAdminRole) {
            event.sendMessage("권한이 없습니다.")
            return
        }
        action()
    }

    private fun enterChannel(event: SlashCommandInteractionEvent) {
        try {
            manualForumController.onChannelEnter(event)
            event.sendMessage("${event._member.effectiveName}님이 ${event.channel.name} 스터디에 참여하였습니다.")
        } catch (e: IllegalArgumentException) {
            event.sendMessage(e.message.toString())
        }
    }

    private fun createChannel(event: SlashCommandInteractionEvent) {
        try {
            manualForumController.onChannelCreate(event)
            event.sendMessage("보이스 채널을 생성하였습니다.")
        } catch (e: IllegalArgumentException) {
            event.sendMessage(e.message.toString())
        }
    }
}
