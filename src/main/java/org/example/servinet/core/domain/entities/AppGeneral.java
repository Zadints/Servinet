package org.example.servinet.core.domain.entities;

import javafx.scene.image.Image;

public class AppGeneral {
    private String appName;
    private Image logoApp;

    public Image getLogoApp() {
        return logoApp;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setLogoApp(Image logoApp) {
        this.logoApp = logoApp;
    }
}
