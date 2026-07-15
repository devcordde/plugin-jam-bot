package de.chojo.pluginjam.bot.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("docker")
public class DockerConfig {
    private String host = "unix:///var/run/docker.sock";
    private String certPath = "/home/user/.docker";
    private boolean tlsVerify = false;
    private String registryUsername;
    private String registryPassword;
    private String registryEmail;
    private String registryUrl;
    private String networkName = "plugin-jam-network";
    private String teamServerImage = "plugin-jam-mc-server:latest";

    public String getHost() {
        return host;
    }

    public String getCertPath() {
        return certPath;
    }

    public boolean isTlsVerify() {
        return tlsVerify;
    }

    public String getRegistryUsername() {
        return registryUsername;
    }

    public String getRegistryPassword() {
        return registryPassword;
    }

    public String getRegistryEmail() {
        return registryEmail;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getTeamServerImage() {
        return teamServerImage;
    }
}
