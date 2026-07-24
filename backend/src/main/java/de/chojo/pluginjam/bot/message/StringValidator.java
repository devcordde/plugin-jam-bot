/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.message;

public class StringValidator {
    private static String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\\.\s_-";
    private static int maxLength = 100;

    public static boolean isValidTeamName(String input) {
        return input.length() <= maxLength && input.matches("[" + allowedCharacters + "]+");
    }
}
