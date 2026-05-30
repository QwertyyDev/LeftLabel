package dev.qwery.leftlabel.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.qwery.leftlabel.config.ConfigManager;
import dev.qwery.leftlabel.listener.LabelInjector;
import dev.qwery.leftlabel.resourcepack.PackServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.List;

public class ReloadCommand implements SimpleCommand {

    private static final String PERMISSION = "leftlabel.reload";

    private final ConfigManager configManager;
    private final LabelInjector labelInjector;
    private final PackServer packServer;
    private final Logger logger;

    public ReloadCommand(ConfigManager configManager, LabelInjector labelInjector, PackServer packServer, Logger logger) {
        this.configManager = configManager;
        this.labelInjector = labelInjector;
        this.packServer = packServer;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission(PERMISSION)) {
            source.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /leftlabel <reload|toggle>", NamedTextColor.YELLOW));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                configManager.load();
                packServer.restart();
                source.sendMessage(Component.text("LeftLabel config reloaded.", NamedTextColor.GREEN));
                logger.info("LeftLabel config reloaded by {}.", source);
            }
            case "toggle" -> {
                boolean nowEnabled = !labelInjector.isEnabled();
                labelInjector.setEnabled(nowEnabled);
                source.sendMessage(Component.text(
                        "LeftLabel " + (nowEnabled ? "enabled" : "disabled") + ".",
                        nowEnabled ? NamedTextColor.GREEN : NamedTextColor.RED
                ));
                logger.info("LeftLabel {} by {}.", nowEnabled ? "enabled" : "disabled", source);
            }
            default -> source.sendMessage(Component.text("Usage: /leftlabel <reload|toggle>", NamedTextColor.YELLOW));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(PERMISSION);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length <= 1) {
            return List.of("reload", "toggle");
        }
        return List.of();
    }
}