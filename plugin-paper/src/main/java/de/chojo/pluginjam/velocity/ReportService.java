/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.velocity;

import com.google.gson.Gson;
import de.chojo.pluginjam.payload.Registration;
import org.bukkit.plugin.Plugin;
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
    private final int velocityApi;
    private final int apiPort;

    private ReportService(Plugin plugin) {
        this.plugin = plugin;
        id = Integer.parseInt(System.getProperty("pluginjam.team.id"));
        name = System.getProperty("pluginjam.team.name");
        velocityApi = Integer.parseInt(System.getProperty("pluginjam.port"));
        apiPort = Integer.parseInt(System.getProperty("javalin.port", "30000"));
    }

    public static ReportService create(Plugin plugin, ScheduledExecutorService executor) {
        var service = new ReportService(plugin);
        executor.scheduleAtFixedRate(service, 1, 1, TimeUnit.MINUTES);
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
        var registration = new Registration(id, name, plugin.getServer().getPort(), apiPort);
        var builder = HttpRequest.newBuilder(apiUrl("v1", "server"))
                                 .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(registration)))
                                 .build();
        client.sendAsync(builder, HttpResponse.BodyHandlers.discarding())
              .whenComplete((res, err) -> {
                  if (err == null) {
                      log.info("Registered server at velocity instance");
                  } else {
                      log.error("Could not register", err);
                  }
              });
    }

    private void ping() {
        var registration = new Registration(id, name, plugin.getServer().getPort(), apiPort);
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
        var builder = HttpRequest.newBuilder(queryApiUrl("id=%s&port=%s".formatted(id,
                                                 plugin.getServer()
                                                       .getPort()),
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
        return URI.create("http://localhost:%d/%s".formatted(velocityApi, String.join("/", path)));
    }

    private URI queryApiUrl(String query, String... path) {
        return URI.create("http://localhost:%d/%s?%s".formatted(velocityApi, String.join("/", path), query));
    }
}
