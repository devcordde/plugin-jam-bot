/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.model;

import io.micronaut.serde.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PowerSignalPayloadTest {

    @Test
    void testDeserialization() throws IOException {
        ObjectMapper objectMapper = ObjectMapper.getDefault();
        String json = "{\"signal\": \"START\"}";
        de.chojo.pluginjam.model.payload.PowerSignalPayload dto = objectMapper.readValue(json, de.chojo.pluginjam.model.payload.PowerSignalPayload.class);
        assertNotNull(dto, "DTO should not be null");
        assertNotNull(dto.signal(), "Signal should not be null");
        assertEquals(de.chojo.pluginjam.model.payload.PowerSignalPayload.Signal.START, dto.signal());
    }
}
