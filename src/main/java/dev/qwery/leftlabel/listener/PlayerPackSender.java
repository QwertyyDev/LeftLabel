package dev.qwery.leftlabel.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.qwery.leftlabel.resourcepack.PackServer;
import net.kyori.adventure.text.Component;

public class PlayerPackSender {

    private final ProxyServer proxyServer;
    private final PackServer packServer;

    public PlayerPackSender(ProxyServer proxyServer, PackServer packServer) {
        this.proxyServer = proxyServer;
        this.packServer = packServer;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        String url = packServer.getPackUrl();
        String sha1 = packServer.getPackSha1();

        if (url == null || sha1 == null) return;

        ResourcePackInfo pack = proxyServer.createResourcePackBuilder(url)
                .setHash(hexToBytes(sha1))
                .setPrompt(Component.text("LeftLabel requires this resource pack for the server list label."))
                .setShouldForce(false)
                .build();

        event.getPlayer().sendResourcePackOffer(pack);
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}