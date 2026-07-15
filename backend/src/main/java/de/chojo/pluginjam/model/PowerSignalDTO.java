package de.chojo.pluginjam.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class PowerSignalDTO {
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
