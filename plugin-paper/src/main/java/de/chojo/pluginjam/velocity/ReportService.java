package de.chojo.pluginjam.velocity;

import com.google.gson.Gson;
import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReportService implements Runnable {
    private final Plugin plugin;
    private final HttpClient client = HttpClient.newBuilder().build();
    private final String name;
    private final Gson gson = new Gson();

    private ReportService(Plugin plugin) {
        this.plugin = plugin;
        name = System.getProperty("pluginjam.name");
    }

    public static ReportService create(Plugin plugin, ScheduledExecutorService executor) {
        var service = new ReportService(plugin);
        executor.scheduleAtFixedRate(service, 1, 1, TimeUnit.MINUTES);
        service.register();
        return service;
    }

    @Override
    public void run() {
        ping();
    }

    private void register() {
        var builder = HttpRequest.newBuilder(apiUrl("v1", "server"))
                                 .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(gson)))
                                 .build();
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding());
    }

    private void ping() {
        var builder = HttpRequest.newBuilder(apiUrl("v1", "server"))
                                 .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(gson)))
                                 .build();
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding());
    }

    private void unregister() {
        var builder = HttpRequest.newBuilder(queryApiUrl("name=%s&port=%s".formatted(name,
                                                 plugin.getServer()
                                                       .getPort()),
                                         "v1", "server"))
                                 .DELETE()
                                 .build();
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding());
    }

    public void shutdown() {
        unregister();
    }

    private URI apiUrl(String... path) {
        try {
            return new URI("http", null, "localhost", Integer.parseInt(System.getProperty("pluginjam.port")), String.join("/", path), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private URI queryApiUrl(String query, String... path) {
        try {
            return new URI("http", null, "localhost", Integer.parseInt(System.getProperty("pluginjam.port")), String.join("/", path), query, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
