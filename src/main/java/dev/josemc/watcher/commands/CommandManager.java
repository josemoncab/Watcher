package dev.josemc.watcher.commands;

import ch.qos.logback.classic.Logger;
import dev.josemc.watcher.commands.exceptions.CommandInitializerException;
import dev.josemc.watcher.commands.interfaces.Command;
import dev.josemc.watcher.commands.interfaces.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/*
* Source (https://github.com/Ree6-Applications/Ree6)
* */

/**
 * Clase para el majeno de los comandos y operaciones relacionadas con ellos
 */
public class CommandManager {

    Logger log = (Logger) LoggerFactory.getLogger(CommandManager.class);

    /**
     * Lista de todos los comandos registrados
     */
    static final ArrayList<ICommand> commands = new ArrayList<>();

    /**
     * Constructor usado para registrar todos los comandos
     *
     * @throws CommandInitializerException error cargando el comando.
     * @throws IllegalStateException       error cargando un comando invalido.
     * @throws IllegalAccessException      instancia de Command no es accesible.
     * @throws InstantiationException      no se puede crear instancia de Command.
     * @throws NoSuchMethodException       constructor de Command no encontrado.
     * @throws InvocationTargetException   instancia del constructor del Command no encontrado.
     */
    public CommandManager() throws CommandInitializerException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        log.info("Cargando comandos...");

        Reflections reflections = new Reflections("dev.josemc.watcher.commands");
        Set<Class<? extends ICommand>> classes = reflections.getSubTypesOf(ICommand.class);

        for (Class<? extends ICommand> aClass : classes) {
            log.info("Cargando comando {}", aClass.getSimpleName());
            addCommand(aClass.getDeclaredConstructor().newInstance());
        }
    }

    /**
     * Metodo para cargar todos los comandos como SlashCommands.
     *
     * @param jda Instancia del bot.
     */
    public CommandListUpdateAction addSlashCommand(JDA jda) {
        CommandListUpdateAction listUpdateAction = jda.updateCommands();

        for (ICommand command : getCommands()) {
            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            CommandData commandData;

            if (command.getCommandData() != null) {
                commandData = command.getCommandData();
            } else {
                commandData = new CommandDataImpl(command.getClass().getAnnotation(Command.class).name(), command.getClass().getAnnotation(Command.class).description());
            }

            if (commandData instanceof CommandDataImpl commandData1) {
                String description = command.getClass().getAnnotation(Command.class).description();

                commandData1.setDescription(description);
                commandData1.setGuildOnly(true);

                listUpdateAction.addCommands(commandData1);
            } else {
                commandData.setGuildOnly(true);

                listUpdateAction.addCommands(commandData);
            }
        }

        return listUpdateAction;
    }

    /**
     * Añade un comando a la lista.
     *
     * @param command {@link ICommand}.
     * @throws CommandInitializerException error al cargar el comando.
     */
    public void addCommand(ICommand command) throws CommandInitializerException {
        if (!command.getClass().isAnnotationPresent(Command.class))
            throw new CommandInitializerException(command.getClass());

        if (!commands.contains(command)) {
            commands.add(command);
        }
    }

    /**
     * Obtener un comando por nombre.
     *
     * @param name nombre del comando.
     * @return el {@link ICommand} con el mismo nombre.
     */
    public ICommand getCommandByName(String name) {
        return getCommands().stream().filter(command -> command.getClass().getAnnotation(Command.class).name().equalsIgnoreCase(name) || Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(name))).findFirst().orElse(null);

    }

    /**
     * Obtener comando por el nombre slash.
     *
     * @param name nombre del comando.
     * @return el {@link ICommand} con el mismo nombre.
     */
    public ICommand getCommandBySlashName(String name) {
        return getCommands().stream().filter(command -> (command.getCommandData() != null && command.getCommandData().getName().equalsIgnoreCase(name)) || (command.getClass().isAnnotationPresent(Command.class) && command.getClass().getAnnotation(Command.class).name().equalsIgnoreCase(name))).findFirst().orElse(null);
    }

    /**
     * Eliminar un comando de la lista.
     *
     * @param command comando que a eliminar.
     */
    public void removeCommand(ICommand command) {
        commands.remove(command);
    }

    /**
     * Obtener todos los comandos de la lista.
     *
     * @return a {@link ArrayList} con todos los comandos.
     */
    public ArrayList<ICommand> getCommands() {
        return commands;
    }

    /**
     * Intentar ejecutar el comando.
     *
     * @param textChannel                  the TextChannel where the command has been performed.
     * @param slashCommandInteractionEvent the Slash Command Event if it was a Slash Command.
     * @return true, if a command has been performed.
     */
    public boolean perform(MessageChannelUnion textChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {
        return performSlashCommand(textChannel, slashCommandInteractionEvent);
    }

    /**
     * Call when a slash command has been performed.
     *
     * @param textChannel                  the TextChannel where the command has been performed.
     * @param slashCommandInteractionEvent the Slash-Command Event.
     * @return true, if a command has been performed.
     */
    private boolean performSlashCommand(MessageChannelUnion textChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {
        //Get the Command by the Slash Command Name.
        ICommand command = getCommandBySlashName(slashCommandInteractionEvent.getName());

        // Check if there is a command with that Name.
        if (command == null || slashCommandInteractionEvent.getGuild() == null || slashCommandInteractionEvent.getMember() == null) {
            sendMessage("command.perform.notFound", null, slashCommandInteractionEvent.getHook().setEphemeral(true));
            return false;
        }

        // Perform the Command.
        command.onASyncPerform(new CommandEvent(command.getClass().getAnnotation(Command.class).name(), slashCommandInteractionEvent.getMember(), null, textChannel, null, slashCommandInteractionEvent));

        return true;
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param commandEvent      the Command-Event.
     */
    public void sendMessage(MessageCreateData messageCreateData, CommandEvent commandEvent) {
        sendMessage(messageCreateData, commandEvent.getChannel(), commandEvent.getInteractionHook());
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param messageChannel    the Message-Channel.
     */
    public void sendMessage(MessageCreateData messageCreateData, MessageChannel messageChannel) {
        sendMessage(messageCreateData, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param messageChannel    the Message-Channel.
     * @param interactionHook   the Interaction-hook if it is a slash command.
     */
    public void sendMessage(MessageCreateData messageCreateData, MessageChannel messageChannel, InteractionHook interactionHook) {
        if (interactionHook == null) {
            if (messageChannel.canTalk()) messageChannel.sendMessage(messageCreateData).queue();
        } else interactionHook.sendMessage(messageCreateData).queue();
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param messageCreateData the Message content.
     * @param messageChannel    the Message-Channel.
     * @param interactionHook   the Interaction-hook if it is a slash command.
     * @param deleteSecond      the delete delay
     */
    public void sendMessage(MessageCreateData messageCreateData, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        if (interactionHook == null) {
            if (messageChannel == null) return;
            if (messageChannel.canTalk())
                messageChannel.sendMessage(messageCreateData).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                    if (message != null && message.getChannel().retrieveMessageById(message.getId()).complete() != null) {
                        return message.delete();
                    }

                    return null;
                }).queue();
        } else {
            interactionHook.sendMessage(messageCreateData).queue();
        }
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message        the Message content.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(String message, MessageChannel messageChannel) {
        sendMessage(message, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message         the Message content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(String message, MessageChannel messageChannel, InteractionHook interactionHook) {
        sendMessage(new MessageCreateBuilder().setContent(message).build(), messageChannel, interactionHook);
    }


    /**
     * Send an Embed to a special Message-Channel.
     *
     * @param embedBuilder   the Embed content.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel) {
        sendMessage(embedBuilder, messageChannel, null);
    }

    /**
     * Send an Embed to a special Message-Channel.
     *
     * @param embedBuilder    the Embed content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel, InteractionHook interactionHook) {
        sendMessage(new MessageCreateBuilder().setEmbeds(embedBuilder.build()).build(), messageChannel, interactionHook);
    }

    /**
     * Delete a specific message.
     *
     * @param message         the {@link Message} entity.
     * @param interactionHook the Interaction-hook, if it is a slash event.
     */
    public void deleteMessage(Message message, InteractionHook interactionHook) {
        if (message != null && message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) && message.getChannel().retrieveMessageById(message.getIdLong()).complete() != null && message.getType().canDelete() && !message.isEphemeral() && interactionHook == null) {
            message.delete().onErrorMap(throwable -> {
                log.error("[CommandManager] Couldn't delete a Message!", throwable);
                return null;
            }).queue();
        }
    }
}
