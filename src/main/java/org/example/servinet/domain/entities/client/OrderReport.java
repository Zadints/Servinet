package org.example.servinet.domain.entities.client;

import org.example.servinet.domain.enums.OrderStatus;

import java.time.LocalDateTime;

public class OrderReport {

    private String reportClientDni;
    private String reportObservation;
    private byte[] imgRouter;
    private byte[] imgConexion;
    private byte[] imgMiniAntenna;
    private String routerPassword;
    private LocalDateTime reportCreate;
    private OrderStatus reportStatus;
    private LocalDateTime reportEnded;

    public OrderReport(String reportClientDni, String reportObservation, byte[] imgRouter, byte[] imgConexion, byte[] imgMiniAntenna, String routerPassword, LocalDateTime reportCreate, OrderStatus reportStatus, LocalDateTime reportEnded) {
        this.reportClientDni = reportClientDni;
        this.reportObservation = reportObservation;
        this.imgRouter = imgRouter;
        this.imgConexion = imgConexion;
        this.imgMiniAntenna = imgMiniAntenna;
        this.routerPassword = routerPassword;
        this.reportCreate = reportCreate;
        this.reportStatus = reportStatus;
        this.reportEnded = reportEnded;
    }

    public String getReportClientDni() {
        return reportClientDni;
    }

    public String getReportObservation() {
        return reportObservation;
    }

    public byte[] getImgRouter() {
        return imgRouter;
    }

    public byte[] getImgConexion() {
        return imgConexion;
    }

    public byte[] getImgMiniAntenna() {
        return imgMiniAntenna;
    }

    public String getRouterPassword() {
        return routerPassword;
    }

    public LocalDateTime getReportCreate() {
        return reportCreate;
    }

    public OrderStatus getReportStatus() {
        return reportStatus;
    }

    public LocalDateTime getReportEnded() {
        return reportEnded;
    }
}