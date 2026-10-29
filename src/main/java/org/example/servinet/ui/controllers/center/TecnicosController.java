package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.example.servinet.application.usecase.TechnicalUseCase;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.technicians.Order;
import org.example.servinet.domain.entities.technicians.Technical;
import org.example.servinet.domain.enums.TechnicalStatus;
import org.example.servinet.ui.controllers.components.TechnicalCardController;
import org.example.servinet.ui.controllers.components.TechnicalHistoryController;

import org.example.servinet.application.security.PermissionPolicy;
import org.example.servinet.domain.enums.Permission;

import java.io.IOException;
import java.util.List;

public class TecnicosController {
    @FXML private ComboBox<TechnicalStatus> cmbTechnicalFilter;
    @FXML private VBox technicianList;
    @FXML private GridPane technicianStatsSection;
    @FXML private VBox technicianListSection;
    @FXML private VBox technicianDetailsSection;
    @FXML private VBox technicianProfileSection;
    @FXML private VBox technicianHistorySection;

    @FXML private StackPane emptyHistoryContainer;

    // Permisos evaluados una vez al cargar la pantalla
    private boolean puedeSeleccionar;
    private boolean puedeDetalles;
    private boolean puedeHistorial;

    @FXML private Label lblActiveTechnicians;
    @FXML private Label lblAvailableTechnicians;
    @FXML private Label lblTechniciansWorking;

    /*Information technical*/
    @FXML private Label lblTechnicianName;
    @FXML private Label lblTechnicianEmail;
    @FXML private Label lblTechnicianPhone;
    @FXML private Label lblTechnicianUuid;

    @FXML private Label lblNoHistory;
    @FXML private Label lblCurrentInstallation;
    @FXML private Label lblCurrentInstallationStatus;
    @FXML private Label lblCurrentClient;
    @FXML private Label lblCurrentLocation;
    @FXML private Button btnRefreshHistorial;
    @FXML private VBox installationHistoryList;

    private final TechnicalUseCase technicalUseCase;
    private Technical actualRenderTechnical;

    public TecnicosController(TechnicalUseCase technicalUseCase) {
        this.technicalUseCase = technicalUseCase;
    }


    @FXML
    protected void initialize() {

    }

    private void show(Node componente, boolean permitido) {

        componente.setVisible(permitido);
        componente.setManaged(permitido);
    }


    @FXML
    protected void clearTechnicalFilters() {

        if (!puedeSeleccionar) {
            return;
        }

        clearTechnicalForm();

        // Recuperar la lista sin filtros.
        loadListTechnical();
    }


    @FXML
    protected void showInstallationHistory() {

        if (!puedeHistorial) {
            return;
        }

        if (actualRenderTechnical == null) {
            return;
        }

        loadInstalledHistory(actualRenderTechnical);
    }


    @FXML
    protected void searchTechnicalFilters() {

        if (!puedeSeleccionar) {
            return;
        }

        TechnicalStatus status = cmbTechnicalFilter.getValue();

        if (status == null) {

            loadListTechnical();
            return;
        }

        loadListTechnical(status);
    }


    /*
    * Methods from load
    */

    private void loadListTechnical() {
        technicianList.getChildren().clear();

        List<Technical> technicals = technicalUseCase.getAll();

        if (technicals == null || technicals.isEmpty()) {
            showEmptyTechnicians("No hay técnicos registrados");
            return;
        }

        for (Technical t : technicals) {
            loadTechnical(t);
        }
    }

    private void loadListTechnical(TechnicalStatus status) {

        if (status == null) return;

        technicianList.getChildren().clear();

        List<Technical> technicals = technicalUseCase.getAll();

        if (technicals == null || technicals.isEmpty()) {
            showEmptyTechnicians("No hay técnicos registrados");
            return;
        }

        List<Technical> filtered = technicals.stream()
                .filter(t -> t.getStatus() == status)
                .toList();

        if (filtered.isEmpty()) {
            showEmptyTechnicians("No hay técnicos con este estado");
            return;
        }

        for (Technical t : filtered) {
            loadTechnical(t);
        }
    }

    private void showEmptyTechnicians(String message) {
        Label lblEmpty = new Label(message);
        lblEmpty.getStyleClass().add("tertiary-text");
        lblEmpty.setWrapText(true);
        lblEmpty.setMaxWidth(300);
        lblEmpty.setAlignment(Pos.CENTER);
        lblEmpty.setTextAlignment(TextAlignment.CENTER);

        StackPane container = new StackPane(lblEmpty);
        container.setMinHeight(180);
        container.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(container, Priority.ALWAYS);

        technicianList.getChildren().add(container);
    }


    private void loadTechnical(Technical technical){
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/servinet/components/technical-card.fxml")
            );

            Node row = loader.load();
            TechnicalCardController controller = loader.getController();

            controller.setTechnical(technical);

            row.setOnMouseClicked(event -> {

                Technical selectedTechnical = controller.getTechnical();
                renderTechnicalForm(selectedTechnical);
            });

            technicianList.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void renderTechnicalForm(Technical t) {

        if (t == null) {
            return;
        }

        // Verificar que exista alguna función autorizada.
        if (!puedeDetalles && !puedeHistorial) {
            return;
        }

        this.actualRenderTechnical = t;

        // Consultar datos personales e instalación.
        if (puedeDetalles) {

            loadTechnicalInfo(t.getTechnical());

            loadActualInstalled(t.getInstNow());
        }

        // Consultar historial.
        if (puedeHistorial) {

            loadInstalledHistory(t);
        }
    }


    private void loadTechnicalInfo(Staff technical){

        if ( technical == null){
            lblTechnicianName.setText("Error LOADER ...");
            lblTechnicianUuid.setText("Error LOADER ...");
            lblTechnicianPhone.setText("Error LOADER ...");
            lblTechnicianEmail.setText("Error LOADER ...");
            return;
        }

        lblTechnicianName.setText(technical.getDisplay());
        lblTechnicianUuid.setText(technical.getUuid());
        lblTechnicianPhone.setText("No defined");
        lblTechnicianEmail.setText(technical.getEmail());
    }

    private void loadActualInstalled(Order actualOrder) {

        if (actualOrder == null) {
            lblCurrentInstallation.setText("Sin instalación");
            lblCurrentInstallationStatus.setText("Sin actividad");
            lblCurrentClient.setText("Sin información");
            lblCurrentLocation.setText("Sin información");
            return;
        }

        lblCurrentLocation.setText(
                valueOrDefault(actualOrder.getClientAddress(), "Sin dirección")
        );

        lblCurrentClient.setText(
                valueOrDefault(actualOrder.getClientName(), "Sin cliente")
        );

        lblCurrentInstallationStatus.setText(
                valueOrDefault(actualOrder.getOrderStatus(), "Sin estado")
        );

        lblCurrentInstallation.setText(
                valueOrDefault(actualOrder.getOrderId(), "Sin código")
        );
    }
    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void loadInstalledHistory(Technical technical) {

        if (!puedeHistorial || technical == null) {
            return;
        }

        // Limpiar los registros anteriores.
        installationHistoryList.getChildren().clear();

        List<Order> orders = technical.getAllInstLog();

        // Sin historial.
        if (orders == null || orders.isEmpty()) {

            lblNoHistory.setText(
                    "No se encontraron instalaciones realizadas."
            );

            installationHistoryList.getChildren().add(
                    emptyHistoryContainer
            );

            show(emptyHistoryContainer, true);

            return;
        }

        show(emptyHistoryContainer, false);

        // Renderizar cada instalación.
        for (Order order : orders) {

            try {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/org/example/servinet/components/technical-history.fxml"
                        )
                );

                Node row = loader.load();

                TechnicalHistoryController controller =
                        loader.getController();

                controller.setTechnicalHistory(order);

                installationHistoryList.getChildren().add(row);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }


    private void clearTechnicalForm() {

        cmbTechnicalFilter.getSelectionModel().clearSelection();
        this.actualRenderTechnical = null;

        lblTechnicianName.setText("");
        lblTechnicianEmail.setText("");
        lblTechnicianPhone.setText("");
        lblTechnicianUuid.setText("");

        lblCurrentInstallation.setText("");
        lblCurrentInstallationStatus.setText("");
        lblCurrentClient.setText("");
        lblCurrentLocation.setText("");

        installationHistoryList.getChildren().clear();

        lblNoHistory.setText(
                "Selecciona un técnico para consultar su historial."
        );

        installationHistoryList.getChildren().add(
                emptyHistoryContainer
        );

        show(emptyHistoryContainer, puedeHistorial);
    }
}
