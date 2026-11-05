package org.example.servinet.ui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.UiPermission;
import org.example.servinet.ui.events.ImageConverter;
import org.example.servinet.ui.events.MouseMove;

import java.io.IOException;

import org.example.servinet.ui.navigator.Navigator;

public class IndexController {

    @FXML private Button btnInventario;
    @FXML private  Button btnTecnicosOrder;
    @FXML private Button btnLogs;
    @FXML private BorderPane brPanel;
    @FXML private HBox titleBar;
    @FXML private StackPane modalOverlay;
    @FXML private Circle userImage;
    @FXML private Label lblUserRol;
    @FXML private Label lblUserName;
    @FXML private Label lblAppName;
    @FXML private Button btnLogoutSession;
    @FXML private Button btnMaximize;
    @FXML private VBox root;
    private boolean maximized = false;

    @FXML private Button btnAdministracion;
    @FXML private Button btnAnuncios;
    @FXML private Button btnAntenas;
    //@FXML private Button btnBackups;
    @FXML private Button btnClientes;
    @FXML private Button btnVentas;
    @FXML private Button btnTecnicos;
    @FXML private Button btnDashboard;


    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final GetStaffUseCase staffUseCase;
    private final AuthorizationSecurity authorizationService;

    public IndexController(AppExecutor appExecutor, Navigator navigator, GetStaffUseCase staffUseCase, AuthorizationSecurity authorizationService) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.staffUseCase = staffUseCase;

        this.authorizationService = authorizationService;
    }

    public void initialize() {

        MouseMove newMove = new MouseMove(titleBar);
        newMove.start();
        loadCurrentStaffAccount();
        permissionPolicy();

    }

    private void loadCurrentStaffAccount(){
        Staff staff  = staffUseCase.getCurrentStaff();

        userImage.setFill(
                new ImagePattern(ImageConverter.toImage(staff.getPerfilImg()))
        );

        lblUserRol.setText(staffUseCase.getUserRol().getName());
        lblUserName.setText(staffUseCase.getCurrentStaff().getDisplay());
        lblAppName.setText("Servinet");
    }


    private void permissionPolicy() {
        UiPermission ui = authorizationService.getUiPermission();

        boolean dashboard = ui.isViewDashboard();
        boolean administracion = ui.isViewAdministration();
        boolean anuncios = ui.isViewAnnounces();
        boolean antenas = ui.isViewAntennas();
        boolean logs = ui.isViewDashboard();
        boolean clientes = ui.isViewClients();
        boolean ventas = ui.isViewSales();
        boolean tecnicos = ui.isViewTechnical();

        show(btnDashboard, dashboard);
        show(btnAdministracion, administracion);
        show(btnAnuncios, anuncios);
        show(btnAntenas, antenas);
        show(btnLogs, logs);
        show(btnClientes, clientes);
        show(btnVentas, ventas);
        show(btnTecnicos, tecnicos);
        show(btnTecnicosOrder, tecnicos);
        show(btnInventario,tecnicos);

        int page = dashboard ? 1
                : administracion ? 2
                : anuncios ? 3
                : antenas ? 4
                : clientes ? 5
                : ventas ? 6
                : tecnicos ? 7
                : logs ? 8
                : 0;

        String fxml = switch (page) {
            case 1 -> "dashboard.fxml";
            case 2 -> "administracion.fxml";
            case 3 -> "anuncios.fxml";
            case 4 -> "antenas.fxml";
            case 5 -> "clientes.fxml";
            case 6 -> "ventas.fxml";
            case 7 -> "tecnicos.fxml";
            case 8 -> "logs.fxml";
            default -> null;
        };

        if (fxml != null) {
            navigate(fxml);
        } else {
            System.out.println("El usuario no tiene permisos para acceder.");
        }
    }


    private void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    @FXML protected void onDashboardClick(ActionEvent event){
        navigate("dashboard.fxml");
    }
    @FXML protected void onAdministracionClick(ActionEvent event) {
        navigate("administration.fxml");
    }
    @FXML protected void onAnunciosClick(ActionEvent event) {
        navigate("anuncios.fxml");
    }
    @FXML protected void onAntenasClick(ActionEvent event) {
        navigate("antenas.fxml");
    }
    @FXML protected void onClientesClick(ActionEvent event) {
        navigate("cliente.fxml");
    }
    /*@FXML protected void onBackupsClick(ActionEvent event) {
        navigate("backups.fxml");
    }*/
    @FXML protected void onSellClick(ActionEvent event) {
        navigate("sell.fxml");
    }
    @FXML protected void onTecnicosClick(ActionEvent event){
        navigate("tecnicos.fxml");
    }
    @FXML protected void onTecnicosOrderClick(ActionEvent actionEvent) {
        navigate("tecnicos-order.fxml");
    }
    @FXML protected void onLogsClick(ActionEvent actionEvent) {
        navigate("logs.fxml");
    }
    @FXML protected  void onInventoryClick(ActionEvent actionEvent) {
        navigate("inventory.fxml");
    }

    private void navigate(String file){
        try {
            navigator.navigateToCenter(brPanel, "/org/example/servinet/center/" + file);
        }catch (IOException e){
            System.out.println("error al cargar: " + e.getMessage() + e.getCause());

            e.printStackTrace();

            Throwable cause = e.getCause();

            while (cause != null) {
                System.err.println("CAUSA: " + cause);
                cause = cause.getCause();
            }
        }

    }


    @FXML protected void onLogoutClick(){
       try {
           navigator.backPage();
       }catch (IOException e){
           System.out.println(e.getCause().getMessage());
       }
    }

    @FXML protected void onCloseClick(ActionEvent event) {
        Platform.exit();
    }
    @FXML protected void onMinimizeClick(ActionEvent event) {
        Stage stage = (Stage) brPanel.getScene().getWindow();
        stage.setIconified(true);
    }
    @FXML protected void onMaximizeClick() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();

        if (!maximized) {

            // Maximizar al 100% de la pantalla
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            stage.setX(screen.getMinX());
            stage.setY(screen.getMinY());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());

            root.getStyleClass().add("maximized");

            maximized = true;

        } else {

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            double width = screen.getWidth() * 0.98;
            double height = screen.getHeight() * 0.98;

            stage.setWidth(width);
            stage.setHeight(height);

            stage.setX(
                    screen.getMinX()
                            + (screen.getWidth() - width) / 2
            );

            stage.setY(
                    screen.getMinY()
                            + (screen.getHeight() - height) / 2
            );

            root.getStyleClass().remove("maximized");

            maximized = false;
        }
    }



}