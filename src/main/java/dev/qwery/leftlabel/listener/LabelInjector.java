package dev.qwery.leftlabel.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.qwery.leftlabel.config.ConfigManager;
import dev.qwery.leftlabel.resourcepack.PackServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class LabelInjector {

    private static final Key SPACE_FONT = Key.key("space", "default");

    private final ConfigManager configManager;
    private final PackServer packServer;
    private boolean enabled = true;

    public LabelInjector(ConfigManager configManager, PackServer packServer) {
        this.configManager = configManager;
        this.packServer = packServer;
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

        builder.description(buildInjectedMotd(label, existingMotd));
        event.setPing(builder.build());
    }

    private Component buildInjectedMotd(Component label, Component existingMotd) {
        int labelWidth = measureWidth(label);
        int rewindAmount = labelWidth + 74;

        String spacer = buildSpacingSequence(-rewindAmount) + buildSpacingSequence(74);

        return Component.empty()
                .append(label)
                .append(Component.text(spacer, Style.style().font(SPACE_FONT).build()))
                .append(existingMotd);
    }

    private String buildSpacingSequence(int pixels) {
        if (pixels == 0) return "";

        StringBuilder sb = new StringBuilder();
        int remaining = Math.abs(pixels);
        int sign = pixels < 0 ? -1 : 1;

        int[] steps = {4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1};

        for (int step : steps) {
            while (remaining >= step) {
                sb.append(spaceChar(sign * step));
                remaining -= step;
            }
        }

        return sb.toString();
    }

    private String spaceChar(int width) {
        int codePoint = 0xD0000 + width;
        return new String(Character.toChars(codePoint));
    }

    private int measureWidth(Component component) {
        int total = 0;

        if (component instanceof TextComponent tc) {
            String text = tc.content();
            boolean bold = component.style().hasDecoration(TextDecoration.BOLD);
            for (char c : text.toCharArray()) {
                int w = charWidth(c);
                total += bold ? w + 1 : w;
            }
        }

        for (Component child : component.children()) {
            total += measureWidth(child);
        }

        return total;
    }

    private int charWidth(char c) {
        return switch (c) {
            case 'i', '!', ',', '.', ':', ';', '|', '\'', '`', '\u00a7' -> 2;
            case 'l' -> 3;
            case 'f', 't', 'I', ' ' -> 4;
            case 'k' -> 5;
            default -> 6;
        };
    }
}