/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Docker {
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

    public String host() {
        return host;
    }

    public String certPath() {
        return certPath;
    }

    public boolean tlsVerify() {
        return tlsVerify;
    }

    public String registryUsername() {
        return registryUsername;
    }

    public String registryPassword() {
        return registryPassword;
    }

    public String registryEmail() {
        return registryEmail;
    }

    public String registryUrl() {
        return registryUrl;
    }

    public String networkName() {
        return networkName;
    }

    public String teamServerImage() {
        return teamServerImage;
    }
}