package dev.qwery.leftlabel.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.qwery.leftlabel.config.ConfigManager;
import net.kyori.adventure.text.Component;

public class LabelInjector {

    private static final String REWIND = "\uF880\uF880";
    private static final String ADVANCE_TO_MOTD = "\uF880\uF840\uF820\uF810\uF808\uF804\uF802\uF801";

    private final ConfigManager configManager;
    private boolean enabled = true;

    public LabelInjector(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyPing(ProxyPingEvent event) {
        if (!enabled) return;

        ServerPing ping = event.getPing();
        ServerPing.Builder builder = ping.asBuilder();

        Component label = configManager.getLabelComponent();
        Component existingMotd = ping.getDescriptionComponent();

        Component injected = buildInjectedMotd(label, existingMotd);
        builder.description(injected);

        event.setPing(builder.build());
    }

    private Component buildInjectedMotd(Component label, Component existingMotd) {
        Component rewind = Component.text(REWIND);
        Component advance = Component.text(ADVANCE_TO_MOTD);

        return Component.empty()
                .append(label)
                .append(rewind)
                .append(advance)
                .append(existingMotd);
    }
}