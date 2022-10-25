/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam;

import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.sadu.base.QueryFactory;

public class JamState extends QueryFactory {
    private final Jam jam;
    private boolean active;
    private boolean voting;
    private boolean ended;

    public JamState(Jam jam, boolean active, boolean voting, boolean ended) {
        super(jam);
        this.jam = jam;
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
        if (set("active", active)) {
            this.active = active;
        }
    }

    public void voting(boolean voting) {
        if (set("voting", voting)) {
            this.voting = voting;
        }
    }

    public void ended(boolean ended) {
        if (set("ended", ended)) {
            this.ended = ended;
        }
    }

    public void finish(){
        active(false);
        voting(false);
        ended(true);

        for (var team : jam.teams().teams()) {
                team.delete();
        }
    }

    private boolean set(String column, boolean state) {
        return builder()
                .query("""
                       INSERT INTO jam_state(jam_id, %s) VALUES(?,?)
                       ON CONFLICT(jam_id)
                           DO UPDATE
                               SET %s = excluded.%s
                       """, column, column, column)
                .parameter(p -> p.setInt(jam.jamId()).setBoolean(state))
                .update()
                .sendSync()
                .changed();
    }
}
