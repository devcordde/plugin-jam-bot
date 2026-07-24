/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.model.payload;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TeamMetaUpdatePayload(String projectDescription, String projectUrl) {
}
