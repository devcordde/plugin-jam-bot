package de.chojo.pluginjam.model;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;

@Serdeable
public record FileInfo(String name, String path, boolean isDirectory, boolean readOnly, long size, Instant lastModified) {
}
