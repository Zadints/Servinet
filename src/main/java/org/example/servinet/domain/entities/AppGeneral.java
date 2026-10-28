package org.example.servinet.domain.entities;

import javafx.scene.image.Image;

public class AppGeneral {
    private String appName;
    private byte[] logoApp;

    public AppGeneral(String appName, byte[] logoApp) {
        this.appName = appName;
        this.logoApp = logoApp;
    }

    public byte[] getLogoApp() {
        return logoApp;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setLogoApp(byte[] logoApp) {
        this.logoApp = logoApp;
    }
}
