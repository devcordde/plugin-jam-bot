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
    private boolean tlsVerify = true;
    private String registryUsername;
    private String registryPassword;
    private String registryEmail;
    private String registryUrl;
    private String networkName = "plugin-jam-network";

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

}