package de.chojo.gamejam.util;

import org.slf4j.Logger;

import java.util.function.BiConsumer;

import static org.slf4j.LoggerFactory.getLogger;

public class Future {
    private static final Logger log = getLogger(Future.class);

    public static Object log(Runnable run){
        run.run();
        return null;
    }

    public static void error(Throwable err){
        log.error(LogNotify.NOTIFY_ADMIN, "Unhandled Exception occured", err);
    }
    public static <T> BiConsumer<T, Throwable> error(){
        return (value, err) -> {
            log.error(LogNotify.NOTIFY_ADMIN, "Unhandled Exception occured", err);
        };
    }
}
