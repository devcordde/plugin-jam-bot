package de.chojo.gamejam.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogNotify {
    /**
     * Will be send to error-log channel.
     */
    public static final Marker NOTIFY_ADMIN = createMarker("NOTIFY_ADMIN");
    /**
     * Will be send to status-log.
     */
    public static final Marker STATUS = createMarker("STATUS");
    /**
     * Currently unused.
     */
    public static final Marker DISCORD = createMarker("DISCORD");

    private static Marker createMarker(@NotNull String name, @NotNull Marker... children) {
        var marker = MarkerFactory.getMarker(name);
        for (var child : children) {
            marker.add(child);
        }
        return marker;
    }
}
