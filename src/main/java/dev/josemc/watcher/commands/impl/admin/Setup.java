package dev.josemc.watcher.commands.impl.admin;

import dev.josemc.watcher.commands.CommandEvent;
import dev.josemc.watcher.commands.interfaces.Command;
import dev.josemc.watcher.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "setup", description = "Configurar el bot")
public class Setup implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {
        
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
