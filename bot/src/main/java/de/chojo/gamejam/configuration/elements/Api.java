package de.chojo.gamejam.configuration.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Api {
    private String host = "localhost";
    private int port = 8888;
    private String token = "letmein";

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String token() {
        return token;
    }
}
