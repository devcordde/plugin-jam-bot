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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerRegistry implements Runnable {
    private static final Logger log = getLogger(ServerRegistry.class);
    private final Map<Integer, Registration> ids = new HashMap<>();
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
        var reg = ids.get(registration.id());

        if (reg != null && !reg.equals(registration)) {
            throw AlreadyRegisteredException.forName(registration.name());
        }

        reg = ports.get(registration.port());

        if (reg != null && !reg.equals(registration)) {
            throw AlreadyRegisteredException.forPort(registration.port());
        }

        log.info("Registered server {} on port {}", registration.name(), registration.port());

        ids.put(registration.id(), registration);
        ports.put(registration.port(), registration);

        proxy.registerServer(new ServerInfo(registration.name(), new InetSocketAddress("localhost", registration.port())));
        ping(registration);
    }

    public void ping(Registration registration) {
        log.debug("Ping of server {} received.", registration.name());
        seen.put(registration, Instant.now());
        if (!ids.containsKey(registration.id()) && !ports.containsKey(registration.port())) {
            log.info("Received ping of unknown server {} with id {}", registration.id(), registration.name());
            log.info("Attempting to register server.");
            register(registration);
        }
    }

    public void unregister(Registration registration) {
        var removed = ids.remove(registration.id());
        if(removed == null){
            log.warn("Unregistered server {} from port {}, but this server is not known.", registration.id(), registration.port());
            return;
        }
        ports.remove(removed.port());
        seen.remove(removed);
        log.info("Unregistered server {} on port {}", removed.name(), removed.port());
        proxy.unregisterServer(new ServerInfo(removed.name(), new InetSocketAddress("localhost", removed.port())));
    }

    public Collection<Registration> server() {
        return ids.values();
    }
}
