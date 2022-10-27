/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempFile {
    public static Path createFile(String prefix, String suffix) throws IOException {
        var tempFile = Files.createTempFile(prefix, suffix);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    public static Path createPath(String prefix, String suffix) throws IOException {
        var tempFile = Files.createTempFile(prefix, suffix);
        tempFile.toFile().delete();
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}
