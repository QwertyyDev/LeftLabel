package dev.qwery.leftlabel.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Path dataDirectory;
    private final Logger logger;

    private String rawLabel;
    private Component labelComponent;
    private int packPort;
    private String packHost;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void load() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.yml");

            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml");
                     OutputStream out = Files.newOutputStream(configFile)) {
                    if (in != null) {
                        in.transferTo(out);
                    }
                }
            }

            Yaml yaml = new Yaml();
            try (InputStream in = Files.newInputStream(configFile)) {
                Map<String, Object> data = yaml.load(in);
                if (data != null) {
                    rawLabel = data.containsKey("label") ? String.valueOf(data.get("label")) : "<gradient:gold:yellow>[LABEL]</gradient>";
                    packPort = data.containsKey("pack-port") ? (int) data.get("pack-port") : 8765;
                    packHost = data.containsKey("pack-host") ? String.valueOf(data.get("pack-host")) : "auto";
                } else {
                    rawLabel = "<gradient:gold:yellow>[LABEL]</gradient>";
                    packPort = 8765;
                    packHost = "auto";
                }
            }

            labelComponent = MINI_MESSAGE.deserialize(rawLabel);

        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
            rawLabel = "<gradient:gold:yellow>[LABEL]</gradient>";
            packPort = 8765;
            packHost = "auto";
            labelComponent = MINI_MESSAGE.deserialize(rawLabel);
        }
    }

    public Component getLabelComponent() {
        return labelComponent;
    }

    public String getRawLabel() {
        return rawLabel;
    }

    public int getPackPort() {
        return packPort;
    }

    public String getPackHost() {
        return packHost;
    }
}