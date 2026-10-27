package org.example.servinet.domain.entities;

public class UiPermission {
    private boolean viewDashboard;
    private boolean viewAdministration;
    private boolean viewAnnounces;
    private boolean viewAntennas;
    private boolean viewSales;
    private boolean viewBackups;
    private boolean viewClients;
    private boolean viewTechnical;


    public UiPermission(boolean viewDashboard, boolean viewAdministration, boolean viewAnnounces, boolean viewAntennas, boolean viewSales, boolean viewBackups, boolean viewClients, boolean viewTechnical) {
        this.viewDashboard = viewDashboard;
        this.viewAdministration = viewAdministration;
        this.viewAnnounces = viewAnnounces;
        this.viewAntennas = viewAntennas;
        this.viewSales = viewSales;
        this.viewBackups = viewBackups;
        this.viewClients = viewClients;
        this.viewTechnical = viewTechnical;
    }


    public boolean isViewDashboard() {
        return viewDashboard;
    }

    public boolean isViewAdministration() {
        return viewAdministration;
    }

    public boolean isViewAnnounces() {
        return viewAnnounces;
    }

    public boolean isViewAntennas() {
        return viewAntennas;
    }

    public boolean isViewBackups() {
        return viewBackups;
    }

    public boolean isViewClients() {
        return viewClients;
    }

    public boolean isViewTechnical() {
        return viewTechnical;
    }

    public boolean isViewSales() {
        return viewSales;
    }
}
