package de.chojo.gamejam.data.wrapper.jam;

public class JamState {
    private boolean active;
    private boolean voting;
    private boolean ended;

    public JamState(boolean active, boolean voting, boolean ended) {
        this.active = active;
        this.voting = voting;
        this.ended = ended;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVoting() {
        return voting;
    }

    public boolean hasEnded() {
        return ended;
    }

    public void active(boolean active) {
        this.active = active;
    }

    public void voting(boolean voting) {
        this.voting = voting;
    }

    public void ended(boolean ended) {
        this.ended = ended;
    }
}
