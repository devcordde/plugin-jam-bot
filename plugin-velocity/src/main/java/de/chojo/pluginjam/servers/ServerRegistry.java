/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.servers;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.chojo.pluginjam.payload.Registration;
import de.chojo.pluginjam.servers.exceptions.AlreadyRegisteredException;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerRegistry implements Runnable {
    private static final Logger log = getLogger(ServerRegistry.class);
    private final Map<String, Registration> names = new HashMap<>();
    private final Map<Integer, Registration> ports = new HashMap<>();
    private final Map<Registration, Instant> seen = new HashMap<>();

    private final ProxyServer proxy;

    private ServerRegistry(ProxyServer proxy) {
        this.proxy = proxy;
    }

    public static ServerRegistry create(ProxyServer proxy, ScheduledExecutorService executor) {
        var registry = new ServerRegistry(proxy);
        executor.scheduleAtFixedRate(registry, 1, 1, TimeUnit.MINUTES);
        return registry;
    }

    @Override
    public void run() {
        var timeout = Instant.now().minus(2, ChronoUnit.MINUTES);
        for (var entry : new HashMap<>(seen).entrySet()) {
            if (entry.getValue().isBefore(timeout)) {
                log.info("Server {}:{} timed out.", entry.getKey().name(), entry.getKey().port());
                unregister(entry.getKey());
            }
        }
    }

    public void register(Registration registration) {
        var reg = names.get(registration.name());

        if (reg != null && !reg.equals(registration)) {
            throw AlreadyRegisteredException.forName(registration.name());
        }

        reg = ports.get(registration.port());

        if (reg != null && !reg.equals(registration)) {
            throw AlreadyRegisteredException.forPort(registration.port());
        }

        log.info("Registered server {} on port {}", registration.name(), registration.port());

        names.put(registration.name(), registration);
        ports.put(registration.port(), registration);

        proxy.registerServer(new ServerInfo(registration.name(), new InetSocketAddress("localhost", registration.port())));
        ping(registration);
    }

    public void ping(Registration registration) {
        log.debug("Ping of server {} received.", registration.name());
        seen.put(registration, Instant.now());
    }

    public void unregister(Registration registration) {
        log.info("Unregistered server {} on port {}", registration.name(), registration.port());
        names.remove(registration.name());
        ports.remove(registration.port());
        seen.remove(registration);
        proxy.unregisterServer(new ServerInfo(registration.name(), new InetSocketAddress("localhost", registration.port())));
    }
}
