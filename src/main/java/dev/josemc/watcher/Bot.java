package dev.josemc.watcher;

import ch.qos.logback.classic.Logger;
import dev.josemc.watcher.commands.CommandManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Bot {
    private final ShardManager shardManager;
    private final Dotenv envVars;
    public static Bot getInstance() {
        return instance;
    }
    private static Bot instance;
    public static Config getConfig() {
        return config;
    }
    public CommandManager getCommandManager() {
        return commandManager;
    }
    private static Config config;
    private CommandManager commandManager;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Bot.class);

    private Bot() throws LoginException, IOException {
        instance = this;
        logger.info("Cargando la configuracion del bot...");
        config = new Config();
        logger.info("Configuracion cargada!");
        envVars = Dotenv.configure().load();
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(envVars.get("TOKEN"));
        builder.setStatus(OnlineStatus.fromKey(config.getString("status")));
        builder.setActivity(Activity.of(Activity.ActivityType.valueOf(config.getString("activity").toUpperCase()), config.getString("activity_message")));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES);
        shardManager = builder.build();
        shardManager.addEventListener(new OtherEvents());
        try {
            instance.commandManager = new CommandManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            new Bot();
        } catch (LoginException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
