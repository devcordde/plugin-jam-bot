package de.chojo.pluginjam.bot.message;

public class StringValidator {
    private static String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\\._-";
    private static int maxLength = 100;

    public static boolean isValidTeamName(String input) {
        return input.length() <= maxLength && input.matches("[" + allowedCharacters + "]+");
    }
}
