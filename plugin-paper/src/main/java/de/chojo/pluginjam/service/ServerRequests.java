/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import de.chojo.pluginjam.payload.RequestsPayload;

public class ServerRequests {
    private boolean restartByUserOrServer = false;
    private boolean restartByEmpty = false;

    public RequestsPayload get() {
        return new RequestsPayload(restartByUserOrServer || restartByEmpty);
    }

    public void restartByUserOrServer(boolean state) {
        restartByUserOrServer = state;
    }

    public void restartByEmpty(boolean state) {
        restartByEmpty = state;
    }
}
