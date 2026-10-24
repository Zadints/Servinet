package org.example.servinet.application.dto;

import javafx.scene.image.Image;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.PlanStatusClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderReportDto {

    // == ORDERS ====================

    private String orderId;

    private String orderClientDni;
    private String orderPlanId;
    private UUID orderReportId;

    private LocalDateTime orderCreate;
    private LocalDateTime orderStartInstall;
    private LocalDateTime orderEndInstall;
    private Integer orderElapsedMinutes;
    private String orderStatus;


    // ==================== CLIENTS ===============

    private String clientDni;
    private String clientName;
    private String clientLastName;
    private String clientPhone;
    private String clientAddress;
    private String clientEmail;
    private String clientWhatsapp;
    private LocalDateTime clientRegister;

    private ClientStatus clientStatus;


    // ======== PLANS ====================

    private String planId;
    private String planName;
    private LocalDateTime planCreated;
    private Integer planSpeed;
    private BigDecimal planPrice;
    private Boolean planIspromo;


    // ============= ORDERS REPORT ====================

    private UUID reportId;

    private UUID technicianId;
    private String reportObservation;
    private UUID reportAntennaConnect;

    private LocalDateTime reportCreate;
    private OrderStatus reportStatus;
    private LocalDateTime reportEnded;

    private BigDecimal reportLatitude;
    private BigDecimal reportLongitude;

    // ==================== PAYMETN ==============
    private PlanStatusClient paymentStatus;

    private String imgUuid;
    private Image imgRouter;
    private Image imgMiniant;
    private Image imgAdditional;

    public String getImgUuid() {
        return imgUuid;
    }

    public void setImgUuid(String imgUuid) {
        this.imgUuid = imgUuid;
    }

    public Image getImgRouter() {
        return imgRouter;
    }

    public void setImgRouter(Image imgRouter) {
        this.imgRouter = imgRouter;
    }

    public Image getImgMiniant() {
        return imgMiniant;
    }

    public void setImgMiniant(Image imgMiniant) {
        this.imgMiniant = imgMiniant;
    }

    public Image getImgAdditional() {
        return imgAdditional;
    }

    public void setImgAdditional(Image imgAdditional) {
        this.imgAdditional = imgAdditional;
    }

    public OrderReportDto() {
    }


    // ==================== GETTERS AND SETTERS ====================
    public PlanStatusClient getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PlanStatusClient paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderClientDni() {
        return orderClientDni;
    }

    public void setOrderClientDni(String orderClientDni) {
        this.orderClientDni = orderClientDni;
    }

    public String getOrderPlanId() {
        return orderPlanId;
    }

    public void setOrderPlanId(String orderPlanId) {
        this.orderPlanId = orderPlanId;
    }

    public UUID getOrderReportId() {
        return orderReportId;
    }

    public void setOrderReportId(UUID orderReportId) {
        this.orderReportId = orderReportId;
    }

    public LocalDateTime getOrderCreate() {
        return orderCreate;
    }

    public void setOrderCreate(LocalDateTime orderCreate) {
        this.orderCreate = orderCreate;
    }

    public LocalDateTime getOrderStartInstall() {
        return orderStartInstall;
    }

    public void setOrderStartInstall(LocalDateTime orderStartInstall) {
        this.orderStartInstall = orderStartInstall;
    }

    public LocalDateTime getOrderEndInstall() {
        return orderEndInstall;
    }

    public void setOrderEndInstall(LocalDateTime orderEndInstall) {
        this.orderEndInstall = orderEndInstall;
    }

    public Integer getOrderElapsedMinutes() {
        return orderElapsedMinutes;
    }

    public void setOrderElapsedMinutes(Integer orderElapsedMinutes) {
        this.orderElapsedMinutes = orderElapsedMinutes;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public ClientStatus getClientStatus() {
        return clientStatus;
    }

    public void setClientStatus(ClientStatus clientStatus) {
        this.clientStatus = clientStatus;
    }
    public String getClientDni() {
        return clientDni;
    }

    public void setClientDni(String clientDni) {
        this.clientDni = clientDni;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientWhatsapp() {
        return clientWhatsapp;
    }

    public void setClientWhatsapp(String clientWhatsapp) {
        this.clientWhatsapp = clientWhatsapp;
    }

    public LocalDateTime getClientRegister() {
        return clientRegister;
    }

    public void setClientRegister(LocalDateTime clientRegister) {
        this.clientRegister = clientRegister;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public LocalDateTime getPlanCreated() {
        return planCreated;
    }

    public void setPlanCreated(LocalDateTime planCreated) {
        this.planCreated = planCreated;
    }

    public Integer getPlanSpeed() {
        return planSpeed;
    }

    public void setPlanSpeed(Integer planSpeed) {
        this.planSpeed = planSpeed;
    }

    public double getPlanPrice() {
        return planPrice.doubleValue();
    }

    public void setPlanPrice(BigDecimal planPrice) {
        this.planPrice = planPrice;
    }

    public Boolean getPlanIspromo() {
        return planIspromo;
    }

    public void setPlanIspromo(Boolean planIspromo) {
        this.planIspromo = planIspromo;
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public UUID getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(UUID technicianId) {
        this.technicianId = technicianId;
    }

    public String getReportObservation() {
        return reportObservation;
    }

    public void setReportObservation(String reportObservation) {
        this.reportObservation = reportObservation;
    }

    public UUID getReportAntennaConnect() {
        return reportAntennaConnect;
    }

    public void setReportAntennaConnect(UUID reportAntennaConnect) {
        this.reportAntennaConnect = reportAntennaConnect;
    }

    public LocalDateTime getReportCreate() {
        return reportCreate;
    }

    public void setReportCreate(LocalDateTime reportCreate) {
        this.reportCreate = reportCreate;
    }

    public OrderStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(OrderStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public LocalDateTime getReportEnded() {
        return reportEnded;
    }

    public void setReportEnded(LocalDateTime reportEnded) {
        this.reportEnded = reportEnded;
    }

    public BigDecimal getReportLatitude() {
        return reportLatitude;
    }

    public void setReportLatitude(BigDecimal reportLatitude) {
        this.reportLatitude = reportLatitude;
    }

    public BigDecimal getReportLongitude() {
        return reportLongitude;
    }

    public void setReportLongitude(BigDecimal reportLongitude) {
        this.reportLongitude = reportLongitude;
    }

}
