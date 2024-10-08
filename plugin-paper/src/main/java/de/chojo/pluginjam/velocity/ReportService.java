/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.velocity;

import com.google.gson.Gson;
import de.chojo.pluginjam.payload.Registration;
import de.eldoria.eldoutilities.utils.ArgumentUtils;
import org.bukkit.plugin.Plugin;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ReportService implements Runnable {
    private static final Logger log = getLogger(ReportService.class);
    private final Plugin plugin;
    private final HttpClient client = HttpClient.newBuilder().build();
    private final int id;
    private final Gson gson = new Gson();
    private final String name;
    private final int velocityPort;
    private final int apiPort;
    private final String velocityHost;
    private final String host = System.getenv("HOSTNAME");

    private ReportService(Plugin plugin) {
        this.plugin = plugin;
        id = Integer.parseInt(System.getProperty("pluginjam.team.id"));
        name = System.getProperty("pluginjam.team.name");
        velocityPort = Integer.parseInt(System.getProperty("pluginjam.port"));
        velocityHost = System.getProperty("pluginjam.host");
        apiPort = Integer.parseInt(System.getProperty("javalin.port", "30000"));
    }

    public static ReportService create(Plugin plugin, ScheduledExecutorService executor) {
        var service = new ReportService(plugin);
        executor.scheduleAtFixedRate(service, 10, 10, TimeUnit.SECONDS);
        service.register();
        return service;
    }

    @Override
    public void run() {
        log.debug("Sending ping");
        ping();
    }

    private void register() {
        log.info("Registering server at velocity instance");
        var registration = new Registration(id, name,  host, plugin.getServer().getPort(), apiPort);
        var builder = HttpRequest.newBuilder(apiUrl("v1", "server"))
                                 .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(registration)))
                                 .build();
        log.info("Posting to {}", builder.uri().toString());
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding())
              .whenComplete((res, err) -> {
                  if (err == null && res.statusCode() == HttpStatus.ACCEPTED_202) {
                      log.info("Registered server at velocity instance.");
                  } else {
                      log.error("Could not register", err);
                  }
              });
    }

    private void ping() {
        var registration = new Registration(id, name, host, plugin.getServer().getPort(), apiPort);
        var builder = HttpRequest.newBuilder(apiUrl("v1", "server"))
                                 .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(registration)))
                                 .build();
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding())
              .whenComplete((res, err) -> {
                  if (err != null) {
                      log.error("Could not send ping to velocity instance", err);
                  }
              });
    }

    private void unregister() {
        log.info("Unregistering server at velocity instance.");
        var builder = HttpRequest.newBuilder(queryApiUrl("id=%s&port=%s&host=%s".formatted(id,
                                                 plugin.getServer()
                                                       .getPort(), host),
                                         "v1", "server"))
                                 .DELETE()
                                 .build();
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding())
              .whenComplete((res, err) -> {
                  if (err != null) {
                      log.error("Could not unregister", err);
                  } else {
                      log.info("Server unregistered at velocity instance");
                  }
              });
    }

    public void shutdown() {
        unregister();
    }

    private URI apiUrl(String... path) {
        return URI.create("http://%s:%d/%s".formatted(velocityHost,velocityPort, String.join("/", path)));
    }

    private URI queryApiUrl(String query, String... path) {
        return URI.create("http://%s:%d/%s?%s".formatted(velocityHost, velocityPort, String.join("/", path), query));
    }
}
