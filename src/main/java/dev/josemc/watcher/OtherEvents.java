package dev.josemc.watcher;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class OtherEvents extends ListenerAdapter {
    @Override
    public void onGuildReady(@Nonnull GuildReadyEvent event) {
       Bot.getInstance().getCommandManager().addSlashCommand(event.getJDA()).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Bot.getInstance().getCommandManager().perform(event.getChannel(), event);
    }
}
