package de.chojo.pluginjam.model;

import io.micronaut.serde.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PowerSignalDTOTest {

    @Test
    void testDeserialization() throws IOException {
        ObjectMapper objectMapper = ObjectMapper.getDefault();
        String json = "{\"signal\": \"START\"}";
        PowerSignalDTO dto = objectMapper.readValue(json, PowerSignalDTO.class);
        assertNotNull(dto, "DTO should not be null");
        assertNotNull(dto.signal(), "Signal should not be null");
        assertEquals(PowerSignalDTO.Signal.START, dto.signal());
    }
}
