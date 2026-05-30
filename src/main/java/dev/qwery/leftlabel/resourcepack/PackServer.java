package dev.qwery.leftlabel.resourcepack;

import com.sun.net.httpserver.HttpServer;
import dev.qwery.leftlabel.config.ConfigManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HexFormat;

public class PackServer {

    private final ConfigManager configManager;
    private final Path dataDirectory;
    private final Logger logger;

    private HttpServer httpServer;
    private byte[] packBytes;
    private String packSha1;
    private String packUrl;

    public PackServer(ConfigManager configManager, Path dataDirectory, Logger logger) {
        this.configManager = configManager;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void start() {
        try {
            packBytes = loadPackBytes();
            packSha1 = sha1Hex(packBytes);

            int port = configManager.getPackPort();
            String host = resolveHost(configManager.getPackHost());
            packUrl = "http://" + host + ":" + port + "/leftlabel_space.zip";

            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/leftlabel_space.zip", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, packBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(packBytes);
                }
            });
            httpServer.start();

            logger.info("LeftLabel resource pack server started on {}:{}", host, port);
            logger.info("Pack URL: {}", packUrl);
            logger.info("Pack SHA1: {}", packSha1);

        } catch (Exception e) {
            logger.error("Failed to start resource pack server", e);
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    public void restart() {
        stop();
        start();
    }

    public String getPackUrl() {
        return packUrl;
    }

    public String getPackSha1() {
        return packSha1;
    }

    private byte[] loadPackBytes() throws IOException {
        Path packFile = dataDirectory.resolve("leftlabel_space.zip");

        if (!Files.exists(packFile)) {
            Files.createDirectories(dataDirectory);
            generatePack(packFile);
        }

        return Files.readAllBytes(packFile);
    }

    private void generatePack(Path destination) throws IOException {
        try (var zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(destination))) {
            writeEntry(zos, "pack.mcmeta", packMcmeta());
            writeEntry(zos, "assets/space/font/default.json", spaceFontJson());
        }
    }

    private void writeEntry(java.util.zip.ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new java.util.zip.ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String packMcmeta() {
        return "{\"pack\":{\"pack_format\":22,\"description\":\"LeftLabel negative space font\"}}"; 
    }

    private String spaceFontJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"providers\":[{\"type\":\"space\",\"advances\":{");

        boolean first = true;
        for (int width = -8192; width <= 8192; width++) {
            if (width == 0) continue;
            int codePoint = 0xD0000 + width;
            String chars = escapeCodePoint(codePoint);
            if (!first) sb.append(",");
            sb.append("\"").append(chars).append("\":").append(width);
            first = false;
        }

        sb.append("}]}");
        return sb.toString();
    }

    private String escapeCodePoint(int codePoint) {
        char[] chars = Character.toChars(codePoint);
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(String.format("\\u%04X", (int) c));
        }
        return sb.toString();
    }

    private String sha1Hex(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(data);
        return HexFormat.of().formatHex(hash);
    }

    private String resolveHost(String configured) {
        if (!configured.equals("auto")) {
            return configured;
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                var addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    var addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}