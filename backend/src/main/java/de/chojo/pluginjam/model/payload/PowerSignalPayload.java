package de.chojo.pluginjam.model.payload;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class PowerSignalPayload {
    public enum Signal {
        START, STOP, RESTART
    }

    private Signal signal;

    public Signal signal() {
        return signal;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }
}
