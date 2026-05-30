package dev.qwery.leftlabel;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.qwery.leftlabel.command.ReloadCommand;
import dev.qwery.leftlabel.config.ConfigManager;
import dev.qwery.leftlabel.listener.LabelInjector;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "leftlabel",
        name = "LeftLabel",
        version = "1.0.0",
        authors = {"dev.qwery"}
)
public class LeftLabel {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager configManager;
    private LabelInjector labelInjector;

    @Inject
    public LeftLabel(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.configManager = new ConfigManager(dataDirectory, logger);
        this.configManager.load();

        this.labelInjector = new LabelInjector(configManager);
        server.getEventManager().register(this, labelInjector);

        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("leftlabel").plugin(this).build(),
                new ReloadCommand(configManager, labelInjector, logger)
        );

        logger.info("LeftLabel enabled.");
    }

    public ProxyServer getServer() {
        return server;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}