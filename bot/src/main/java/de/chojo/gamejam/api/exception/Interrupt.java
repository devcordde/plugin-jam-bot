package de.chojo.gamejam.api.exception;

import io.javalin.http.HttpCode;

public final class Interrupt {
    private Interrupt() {
    }

    public static InterruptException create(String message, HttpCode httpCode){
        return new InterruptException(message, httpCode);
    }

    public static InterruptException notFound(String entity) {
        return create(String.format("%s not found.", entity), HttpCode.NOT_FOUND);
    }

    public static InterruptException noJam() {
        return create("No current or upcoming jam", HttpCode.NOT_FOUND);
    }
}
