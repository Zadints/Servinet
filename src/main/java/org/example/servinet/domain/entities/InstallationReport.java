package org.example.servinet.domain.entities;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class InstallationReport {
    private final String orderId;
    private final String reportClientDni;
    private final String reportObservation;
    private final byte[] imgRouter;
    private final byte[] imgConexion;
    private final byte[] imgMiniAntenna;
    private final String routerPassword;
    private final LocalDateTime reportCreate;
    private final String reportStatus;
    private final LocalDateTime reportEnded;

    public InstallationReport(String orderId, String reportClientDni,
                              String reportObservation, byte[] imgRouter,
                              byte[] imgConexion, byte[] imgMiniAntenna,
                              String routerPassword) {
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        if (reportClientDni == null || !reportClientDni.matches("\\d{8}")) {
            throw new IllegalArgumentException("DNI inválido");
        }
        if (routerPassword == null || routerPassword.isBlank() || routerPassword.length() > 255) {
            throw new IllegalArgumentException("Contraseña de router obligatoria (máximo 255 caracteres)");
        }
        this.reportClientDni = reportClientDni;
        this.reportObservation = reportObservation;
        this.imgRouter = copy(imgRouter);
        this.imgConexion = copy(imgConexion);
        this.imgMiniAntenna = copy(imgMiniAntenna);
        this.routerPassword = routerPassword;
        // SQL Server asigna los valores definitivos mediante DEFAULT al insertar.
        this.reportCreate = null;
        this.reportStatus = "PENDIENTE";
        this.reportEnded = null;
    }

    private static byte[] copy(byte[] data) {
        return data == null ? null : Arrays.copyOf(data, data.length);
    }

    public String getOrderId() { return orderId; }
    public String getReportClientDni() { return reportClientDni; }
    public String getReportObservation() { return reportObservation; }
    public byte[] getImgRouter() { return copy(imgRouter); }
    public byte[] getImgConexion() { return copy(imgConexion); }
    public byte[] getImgMiniAntenna() { return copy(imgMiniAntenna); }
    public String getRouterPassword() { return routerPassword; }
    public LocalDateTime getReportCreate() { return reportCreate; }
    public String getReportStatus() { return reportStatus; }
    public LocalDateTime getReportEnded() { return reportEnded; }
}