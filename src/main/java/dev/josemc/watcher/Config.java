package dev.josemc.watcher;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    final YamlFile yamlFile = new YamlFile("Watcher/config.yml");
    File folder = new File(Paths.get("Watcher").toUri());
    File file = new File(Paths.get(folder.getPath(),"config.yml").toUri());
    public Config() throws IOException {
        if (!file.exists()) {
            folder.mkdirs();
            Files.copy(Bot.class.getResourceAsStream("/config.yml"), file.toPath());
        }
        yamlFile.createOrLoadWithComments();
    }

    public String getString(String path) {
        return yamlFile.getString(path);
    }

    public void set(String path, Object value) {
        yamlFile.set(path, value);
        try {
            yamlFile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
