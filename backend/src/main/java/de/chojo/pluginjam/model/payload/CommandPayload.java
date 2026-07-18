package de.chojo.pluginjam.model.payload;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CommandPayload(String command) {
}
