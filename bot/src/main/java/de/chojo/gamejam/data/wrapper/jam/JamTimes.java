package de.chojo.gamejam.data.wrapper.jam;

import java.time.ZoneId;

public class JamTimes {
    private final ZoneId zone;
    private final TimeFrame total;
    private final TimeFrame registration;
    private final TimeFrame jam;
    private final TimeFrame voting;


    public JamTimes(ZoneId zone, TimeFrame registration, TimeFrame jam) {
        this.zone = zone;
        this.registration = registration;
        this.jam = jam;
        this.voting = voting;
        total = new TimeFrame(registration.start(), jam.end());
    }

    public TimeFrame total() {
        return total;
    }

    public TimeFrame registration() {
        return registration;
    }

    public TimeFrame jam() {
        return jam;
    }

    public TimeFrame voting() {
        return voting;
    }

    public ZoneId zone() {
        return zone;
    }
}
