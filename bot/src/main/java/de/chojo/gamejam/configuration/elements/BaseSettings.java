/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class BaseSettings {
    private String token = "";
    private List<Long> botOwner = new ArrayList<>();

    public String token() {
        return token;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }
}
