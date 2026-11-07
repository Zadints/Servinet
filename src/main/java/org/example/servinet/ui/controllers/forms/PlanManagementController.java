package org.example.servinet.ui.controllers.forms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.exception.InvalidValueException;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlanManagementController {
    private final PlanUseCase planUseCase;
    public Button btnClose;

    @FXML private TextField txtPlanName;
    @FXML private TextField txtPlanSpeed;
    @FXML private TextField txtPlanPrice;

    @FXML private CheckBox chkPlanPromo;
    @FXML private CheckBox chkPlanActive;

    @FXML private Label lblErrorPlan;

    @FXML private ListView<ClientPlan> listPlans;

    @FXML private Button btnCreatePlan;
    @FXML private Button btnRefreshPlans;

    private final ObservableList<ClientPlan> plans =
            FXCollections.observableArrayList();

    public PlanManagementController(PlanUseCase planUseCase) {
        this.planUseCase = planUseCase;
    }

    @FXML
    public void initialize() {
        configureList();
        loadPlans();
    }

    // =====================================================
    // CONFIGURACIÓN DEL LISTADO
    // =====================================================

    private void configureList() {

        listPlans.setItems(plans);

        listPlans.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(ClientPlan plan, boolean empty) {
                super.updateItem(plan, empty);

                if (empty || plan == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                VBox information = new VBox(4);

                Label lblName = new Label(plan.getName());
                lblName.getStyleClass().add("primary-text");

                Label lblDetails = new Label(
                        plan.getSpeed() + " Mbps  ·  S/ "
                                + String.format("%.2f", plan.getPrice())
                                + " / mes"
                );
                lblDetails.getStyleClass().add("secondary-text");

                HBox badges = new HBox(8);

                Label lblPromo = new Label(
                        Boolean.TRUE.equals(plan.getPromo())
                                ? "Promoción"
                                : "Sin promoción"
                );

                lblPromo.getStyleClass().add("tertiary-text");

                Label lblStatus = new Label(
                        Boolean.TRUE.equals(plan.getActive())
                                ? "Activo"
                                : "Inactivo"
                );

                lblStatus.getStyleClass().add("tertiary-text");

                badges.getChildren().addAll(lblPromo, lblStatus);

                information.getChildren().addAll(
                        lblName,
                        lblDetails,
                        badges
                );

                HBox.setHgrow(information, Priority.ALWAYS);

                Button btnDelete = new Button("Eliminar");
                btnDelete.getStyleClass().add("secondary-button");

                btnDelete.setOnAction(event -> deletePlan(plan));

                HBox container = new HBox(12);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.getChildren().addAll(information, btnDelete);

                container.getStyleClass().add("admin-option");

                setText(null);
                setGraphic(container);
            }
        });
    }

    // =====================================================
    // CARGAR PLANES
    // =====================================================

    @FXML
    public void loadPlans() {

        try {
            var result = planUseCase.getAll();

            plans.setAll(
                    result != null ? result : FXCollections.observableArrayList()
            );

            hideError();

        } catch (RuntimeException e) {
            showError("No se pudieron cargar los planes");
        }
    }

    // =====================================================
    // CREAR PLAN
    // =====================================================

    @FXML
    public void createPlan() {

        try {

            String name = validateName();
            Integer speed = validateSpeed();
            Double price = validatePrice();

            ClientPlan plan = new ClientPlan(
                    UUID.randomUUID().toString(),
                    name,
                    LocalDateTime.now(),
                    speed,
                    price,
                    chkPlanPromo.isSelected(),
                    chkPlanActive.isSelected()
            );

            planUseCase.addPlan(plan);

            clearForm();
            loadPlans();

            showInformation(
                    "Plan registrado",
                    "El plan " + name + " se registró correctamente."
            );

        } catch (InvalidValueException e) {
            showError(e.getMessage());

        } catch (RuntimeException e) {
            showError("Ocurrió un error al registrar el plan");
        }
    }

    private String validateName() {

        String name = txtPlanName.getText();

        if (name == null || name.isBlank()) {
            throw new InvalidValueException(
                    "Debes ingresar el nombre del plan"
            );
        }

        name = name.trim().toUpperCase();

        if (name.length() > 8) {
            throw new InvalidValueException(
                    "El nombre del plan no puede superar los 8 caracteres"
            );
        }

        if (!name.matches("[A-Z0-9_]+")) {
            throw new InvalidValueException(
                    "El nombre solo puede contener letras, números y guion bajo"
            );
        }

        return name;
    }

    private Integer validateSpeed() {

        String value = txtPlanSpeed.getText();

        if (value == null || value.isBlank()) {
            throw new InvalidValueException(
                    "Debes ingresar la velocidad del plan"
            );
        }

        try {
            int speed = Integer.parseInt(value.trim());

            if (speed <= 0) {
                throw new InvalidValueException(
                        "La velocidad debe ser mayor que cero"
                );
            }

            return speed;

        } catch (NumberFormatException e) {
            throw new InvalidValueException(
                    "La velocidad debe ser un número entero válido"
            );
        }
    }

    private Double validatePrice() {

        String value = txtPlanPrice.getText();

        if (value == null || value.isBlank()) {
            throw new InvalidValueException(
                    "Debes ingresar el precio del plan"
            );
        }

        try {
            double price = Double.parseDouble(
                    value.trim().replace(",", ".")
            );

            if (!Double.isFinite(price) || price <= 0) {
                throw new InvalidValueException(
                        "El precio debe ser mayor que cero"
                );
            }

            if (java.math.BigDecimal.valueOf(price).scale() > 2) {
                throw new InvalidValueException(
                        "El precio solo puede tener dos decimales"
                );
            }

            return price;

        } catch (NumberFormatException e) {
            throw new InvalidValueException(
                    "Ingresa un precio válido. Ejemplo: 89.90"
            );
        }
    }

    // =====================================================
    // ELIMINAR PLAN
    // =====================================================

    private void deletePlan(ClientPlan plan) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Eliminar plan");
        alert.setHeaderText("¿Deseas eliminar este plan?");
        alert.setContentText(
                "Plan: " + plan.getName()
                        + "\nVelocidad: " + plan.getSpeed() + " Mbps"
                        + "\nPrecio: S/ " + String.format("%.2f", plan.getPrice())
        );

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                try {
                    planUseCase.deletePlan(plan.getId());

                    loadPlans();

                } catch (RuntimeException e) {
                    showError("No se pudo eliminar el plan. Puede estar asociado a una orden.");
                }
            }
        });
    }

    // =====================================================
    // LIMPIAR FORMULARIO
    // =====================================================

    @FXML
    public void clearForm() {

        txtPlanName.clear();
        txtPlanSpeed.clear();
        txtPlanPrice.clear();

        chkPlanPromo.setSelected(false);
        chkPlanActive.setSelected(true);

        hideError();
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void showError(String message) {

        lblErrorPlan.setText(message);
        lblErrorPlan.setVisible(true);
        lblErrorPlan.setManaged(true);
    }

    private void hideError() {

        lblErrorPlan.setVisible(false);
        lblErrorPlan.setManaged(false);
    }

    private void showInformation(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
