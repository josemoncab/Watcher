package dev.josemc.watcher.commands;

import dev.josemc.watcher.Bot;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CommandEvent {
    /**
    * Nombre del comando ejecutado
    * */
    @Getter
    String command;

    /**
     * Usuario que ejecuto el comando
     * */
    @NonNull
    Member member;

    /**
     * El mensaje de la ejecucion del comando
     * */
    @NonNull
    Message message;

    /**
     * Canal donde se ejecuto el comando
     * */
    @NonNull
    MessageChannelUnion channel;

    /**
     * Argumentos para el comando
     * */
    @Nullable
    String[] arguments;

    /**
     * Evento asicoado a la ejecucion del comando
     * */
    @Nullable
    SlashCommandInteractionEvent slashCommandInteractionEvent;

    /**
     * Constructor used to save the Data.
     *
     * @param command                      the Command Name.
     * @param member                       the {@link Member} Entity.
     * @param message                      the {@link Message} Entity.
     * @param arguments                    the given Arguments.
     * @param slashCommandInteractionEvent the {@link SlashCommandInteractionEvent} Entity.
     */
    public CommandEvent(String command, @NonNull Member member, @NonNull Message message, @NonNull MessageChannelUnion channel, @Nullable String[] arguments, @Nullable SlashCommandInteractionEvent slashCommandInteractionEvent) {
        this.command = command;
        this.member = member;
        this.message = message;
        this.channel = channel;
        this.arguments = arguments;
        this.slashCommandInteractionEvent = slashCommandInteractionEvent;
    }

    /**
     * Reply to the Command execution.
     *
     * @param message the Message to reply with.
     */
    public void reply(String message) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setContent(message);
        reply(messageCreateBuilder.build());
    }

    /**
     * Reply to the Command execution.
     *
     * @param message the Message to reply with.
     */
    public void reply(MessageEmbed message) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setEmbeds(message);
        reply(messageCreateBuilder.build());
    }

    /**
     * Reply to the Command execution.
     *
     * @param message the Message to reply with.
     */
    public void reply(MessageCreateData message) {
        Bot.getInstance().getCommandManager().sendMessage(message, getChannel(), getInteractionHook());
    }

    /**
     * Get the {@link Member} Entity associated with the Event.
     *
     * @return the {@link Member} Entity.
     */
    public @NotNull Member getMember() {
        return member;
    }


    /**
     * Get the {@link Message} Entity associated with the Event.
     *
     * @return the {@link Message} Entity.
     */
    public @Nullable Message getMessage() {
        return message;
    }

    /**
     * Get the {@link TextChannel} Entity associated with the Event.
     *
     * @return the {@link TextChannel} Entity.
     */
    public @NotNull MessageChannelUnion getChannel() {
        return channel;
    }

    /**
     * Get the Arguments associated with the Event.
     *
     * @return the Arguments.
     */
    public String[] getArguments() {
        if (arguments == null) {
            arguments = new String[0];
        }
        return arguments;
    }

    /**
     * Get the {@link SlashCommandInteractionEvent} Entity associated with the Event.
     *
     * @return the {@link SlashCommandInteractionEvent} Entity.
     */
    public @Nullable SlashCommandInteractionEvent getSlashCommandInteractionEvent() {
        return slashCommandInteractionEvent;
    }

    /**
     * Check if the Command Execution is a Slash Command or not.
     *
     * @return true, if it is a Slash Command Execution. | false, if not.
     */
    public boolean isSlashCommand() {
        return getSlashCommandInteractionEvent() != null;
    }

    /**
     * Get the {@link InteractionHook} from the {@link SlashCommandInteractionEvent}.
     *
     * @return the {@link InteractionHook} Entity.
     */
    public InteractionHook getInteractionHook() {
        if (isSlashCommand()) return getSlashCommandInteractionEvent().getHook().setEphemeral(true);

        return null;
    }
}
