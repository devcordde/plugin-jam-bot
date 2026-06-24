package de.chojo.pluginjam.bot.util;

public class MentionUtil {

    public static String role(long id) {
        return "<@&" + id + ">";
    }

    public static String user(long id) {
        return "<@" + id + ">";
    }
}
