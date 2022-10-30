/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import de.chojo.pluginjam.payload.RequestsPayload;

public class ServerRequests {
    private boolean restartByCommand = false;
    private boolean restartByEmpty = false;

    public RequestsPayload get() {
        return new RequestsPayload(restartByCommand || restartByEmpty);
    }

    public void restartByCommand(boolean state) {
        restartByCommand = state;
    }

    public void restartByEmpty(boolean state) {
        restartByEmpty = state;
    }
}
