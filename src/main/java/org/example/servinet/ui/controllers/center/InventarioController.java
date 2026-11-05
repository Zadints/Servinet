package org.example.servinet.ui.controllers.center;

import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.servinet.application.dto.InventoryAlert;
import org.example.servinet.application.dto.InventoryItem;
import org.example.servinet.application.dto.InventoryMovement;
import org.example.servinet.application.usecase.inventory.*;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.enums.InventoryCategory;
import org.example.servinet.domain.enums.StockStatus;
import org.example.servinet.ui.animations.ShimmerEffectAnimation;
// IMPORTA AppExecutor y ShimmerEffectAnimation desde los paquetes reales de tu proyecto.

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class InventarioController {

    private final CreateInventoryUseCase createInventoryUseCase;
    private final UpdateInventoryUseCase updateInventoryUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final ManageInventoryStockUseCase manageInventoryStockUseCase;
    private final ManageInventoryEquipmentUseCase manageInventoryEquipmentUseCase;
    private final GetStaffUseCase getStaffUseCase;
    private final AppExecutor appExecutor;
    private final ShimmerEffectAnimation shimmerEffectAnimation;
    private int activeOperations = 0;
    private long refreshVersion = 0;

    @FXML private Label lblTotalItems;
    @FXML private Label lblTotalStock;
    @FXML private Label lblLowStock;
    @FXML private Label lblOutOfStock;
    @FXML private Label lblResults;
    @FXML private Label lblEmptyInventory;
    @FXML private Label lblNoAlerts;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<InventoryCategory> cmbCategory;
    @FXML private ComboBox<String> cmbStockStatus;
    @FXML private VBox itemsContainer;
    @FXML private VBox alertsContainer;

    private List<InventoryItem> items = List.of();

    public InventarioController(
            CreateInventoryUseCase createInventoryUseCase,
            UpdateInventoryUseCase updateInventoryUseCase,
            GetInventoryUseCase getInventoryUseCase,
            ManageInventoryStockUseCase manageInventoryStockUseCase,
            ManageInventoryEquipmentUseCase manageInventoryEquipmentUseCase,
            GetStaffUseCase getStaffUseCase, AppExecutor appExecutor,
            ShimmerEffectAnimation shimmerEffectAnimation
    ) {
        this.createInventoryUseCase = createInventoryUseCase;
        this.updateInventoryUseCase = updateInventoryUseCase;
        this.getInventoryUseCase = getInventoryUseCase;
        this.manageInventoryStockUseCase = manageInventoryStockUseCase;
        this.manageInventoryEquipmentUseCase = manageInventoryEquipmentUseCase;
        this.getStaffUseCase = getStaffUseCase;
        this.appExecutor = appExecutor;
        this.shimmerEffectAnimation = shimmerEffectAnimation;
    }

    @FXML
    private void initialize() {
        cmbCategory.getItems().add(null);
        cmbCategory.getItems().addAll(InventoryCategory.values());
        cmbCategory.setPromptText("Todas las categorías");

        cmbStockStatus.setItems(FXCollections.observableArrayList(
                "TODOS", "AVAILABLE", "LOW_STOCK", "OUT_OF_STOCK"
        ));
        cmbStockStatus.setValue("TODOS");

        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbCategory.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbStockStatus.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        refreshInventory();
    }

    private record InventorySnapshot(List<InventoryItem> items, List<InventoryAlert> alerts) {}

    private void startLoading() {
        if (activeOperations++ == 0) {
            shimmerEffectAnimation.start(
                    lblTotalItems, lblTotalStock, lblLowStock, lblOutOfStock,
                    itemsContainer, alertsContainer
            );
        }
    }

    private void stopLoading() {
        if (activeOperations > 0 && --activeOperations == 0) {
            shimmerEffectAnimation.stop();
        }
    }

    private <T> void executeTask(Task<T> task, java.util.function.Consumer<T> onSuccess) {
        startLoading();
        task.setOnSucceeded(event -> {
            stopLoading();
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            stopLoading();
            Throwable error = task.getException();
            showError(error);
        });
        task.setOnCancelled(event -> stopLoading());
        try {
            appExecutor.execute(task);
        } catch (RuntimeException e) {
            stopLoading();
            showError(e);
        }
    }

    @FXML
    private void refreshInventory() {
        long version = ++refreshVersion;
        Task<InventorySnapshot> task = new Task<>() {
            @Override
            protected InventorySnapshot call() {
                return new InventorySnapshot(
                        getInventoryUseCase.getAllItems(),
                        getInventoryUseCase.getStockAlerts()
                );
            }
        };
        executeTask(task, snapshot -> {
            if (version != refreshVersion) return;
            items = snapshot.items();
            updateStatistics();
            applyFilters();
            loadAlerts(snapshot.alerts());
        });
    }

    private void updateStatistics() {
        lblTotalItems.setText(String.valueOf(items.size()));

        BigDecimal totalStock = items.stream()
                .map(InventoryItem::stock)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalStock.setText(totalStock.stripTrailingZeros().toPlainString());

        long low = items.stream()
                .filter(item -> item.status() == StockStatus.LOW_STOCK)
                .count();

        long out = items.stream()
                .filter(item -> item.status() == StockStatus.OUT_OF_STOCK)
                .count();

        lblLowStock.setText(String.valueOf(low));
        lblOutOfStock.setText(String.valueOf(out));
    }

    private void applyFilters() {
        String query = txtSearch.getText() == null
                ? ""
                : txtSearch.getText().trim().toLowerCase(Locale.ROOT);

        InventoryCategory category = cmbCategory.getValue();
        String status = cmbStockStatus.getValue();

        List<InventoryItem> filtered = items.stream()
                .filter(item -> query.isBlank()
                        || contains(item.name(), query)
                        || contains(item.code(), query)
                        || contains(item.brand(), query)
                        || contains(item.model(), query))
                .filter(item -> category == null || item.category() == category)
                .filter(item -> status == null
                        || status.equals("TODOS")
                        || item.status().name().equals(status))
                .toList();

        renderItems(filtered);
        lblResults.setText(filtered.size() + " artículos encontrados");
    }

    private boolean contains(String value, String query) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void renderItems(List<InventoryItem> filtered) {
        itemsContainer.getChildren().clear();

        lblEmptyInventory.setVisible(filtered.isEmpty());
        lblEmptyInventory.setManaged(filtered.isEmpty());

        for (InventoryItem item : filtered) {
            itemsContainer.getChildren().add(createItemRow(item));
        }
    }

    private HBox createItemRow(InventoryItem item) {
        Label name = new Label(item.name());
        name.getStyleClass().add("secondary-text");

        Label code = new Label(item.code());
        code.getStyleClass().add("secondary-text");

        Label category = new Label(item.category().getDisplayName());
        category.getStyleClass().add("secondary-text");

        Label stock = new Label(
                item.stock().stripTrailingZeros().toPlainString()
                        + " " + item.unit()
        );
        stock.getStyleClass().add("secondary-text");

        Label status = new Label(item.status().name());
        status.getStyleClass().add("secondary-text");

        VBox information = new VBox(5, name, code, category);
        HBox.setHgrow(information, Priority.ALWAYS);

        Button btnEntry = new Button("Entrada");
        btnEntry.getStyleClass().add("secondary-button");

        Button btnExit = new Button("Salida");
        btnExit.getStyleClass().add("secondary-button");

        Button btnEquipment = new Button("Equipos");
        btnEquipment.getStyleClass().add("secondary-button");

        btnEntry.setOnAction(event -> registerMovement(item, true));
        btnExit.setOnAction(event -> registerMovement(item, false));
        btnEquipment.setOnAction(event -> registerEquipment(item));

        if (item.recoverable()) {
            btnEntry.setDisable(true);

            btnExit.setDisable(false);

            btnEquipment.setDisable(false);
        } else {

            btnEntry.setDisable(false);
            btnExit.setDisable(false);

            btnEquipment.setDisable(true);
        }

        HBox row = new HBox(
                16, information, stock, status,
                btnEntry, btnExit , btnEquipment
        );

        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("user-row");

        return row;
    }

    private void loadAlerts(List<InventoryAlert> alerts) {
        alertsContainer.getChildren().clear();

        lblNoAlerts.setVisible(alerts.isEmpty());
        lblNoAlerts.setManaged(alerts.isEmpty());

        for (InventoryAlert alert : alerts) {
            Label label = new Label(
                    alert.name() + " — "
                            + alert.stock().stripTrailingZeros().toPlainString()
                            + " " + alert.unit()
                            + " disponibles"
            );

            label.getStyleClass().add("secondary-text");

            HBox row = new HBox(label);
            row.getStyleClass().add("user-row");

            alertsContainer.getChildren().add(row);
        }
    }

    @FXML
    private void clearFilters() {
        txtSearch.clear();
        cmbCategory.setValue(null);
        cmbStockStatus.setValue("TODOS");
    }

    @FXML
    private void createItem() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Nuevo artículo");
        TextField name = new TextField();
        TextArea description = new TextArea();
        TextField brand = new TextField();
        TextField model = new TextField();
        TextField minimum = new TextField("5");
        TextField price = new TextField("0");

        ComboBox<InventoryCategory> category = new ComboBox<>();
        category.getItems().addAll(InventoryCategory.values());

        ComboBox<String> unit = new ComboBox<>();
        unit.getItems().addAll(
                "UNIDAD", "METRO", "ROLLO", "CAJA", "PAQUETE"
        );
        unit.setValue("UNIDAD");

        CheckBox recoverable = new CheckBox("Equipo recuperable");

        category.valueProperty().addListener((obs, oldValue, value) -> {
            if (value != null) {
                recoverable.setSelected(value.isRecoverable());
                if (value.isRecoverable()) {
                    unit.setValue("UNIDAD");
                }
            }
        });

        VBox content = new VBox(
                9,
                field("Nombre", name),
                field("Descripción", description),
                field("Categoría", category),
                field("Marca", brand),
                field("Modelo", model),
                field("Unidad", unit),
                field("Stock mínimo", minimum),
                field("Precio de compra", price),
                recoverable
        );

        content.setPrefWidth(450);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(480);
        scroll.getStyleClass().add("transparent-scroll");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL, ButtonType.OK
        );

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;

            return new InventoryItem(
                    null,
                    null,
                    name.getText().trim(),
                    description.getText(),
                    category.getValue(),
                    brand.getText(),
                    model.getText(),
                    recoverable.isSelected(),
                    unit.getValue(),
                    BigDecimal.ZERO,
                    new BigDecimal(minimum.getText().trim()),
                    new BigDecimal(price.getText().trim()),
                    true,
                    StockStatus.OUT_OF_STOCK
            );
        });

        try {
            dialog.showAndWait().ifPresent(item -> {
                Task<UUID> task = new Task<>() {
                    @Override
                    protected UUID call() {
                        return createInventoryUseCase.createItem(item);
                    }
                };
                executeTask(task, uuid -> refreshInventory());
            });
        } catch (Exception e) {
            showError(e);
        }
    }

    private VBox field(String title, Control control) {
        Label label = new Label(title);
        label.getStyleClass().add("secondary-text");

        control.setMaxWidth(Double.MAX_VALUE);

        if (control instanceof TextField || control instanceof TextArea) {
            control.getStyleClass().add("secondary-input");
        } else if (control instanceof ComboBox<?>) {
            control.getStyleClass().add("secondary-combo");
        }

        return new VBox(5, label, control);
    }

    private void registerMovement(InventoryItem item, boolean entry) {

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle(entry ? "Entrada de stock" : "Salida de stock");
        dialog.setHeaderText(item.name());
        dialog.setContentText("Cantidad (" + item.unit() + "):");

        dialog.showAndWait().ifPresent(value -> {

            Task<Void> task = new Task<>() {

                @Override
                protected Void call() {

                    BigDecimal quantity = new BigDecimal(value.trim());

                    UUID staffUuid = UUID.fromString(
                            getStaffUseCase.getCurrentStaff().getUuid()
                    );

                    if (entry) {
                        manageInventoryStockUseCase.registerEntry(
                                item.uuid(),
                                quantity,
                                staffUuid,
                                "Entrada registrada desde inventario"
                        );
                    } else {
                        manageInventoryStockUseCase.registerExit(
                                item.uuid(),
                                quantity,
                                staffUuid,
                                null,
                                "Salida registrada desde inventario"
                        );
                    }

                    return null;
                }
            };

            task.setOnSucceeded(event -> {
                refreshInventory();
                showMessage(entry
                        ? "Entrada registrada correctamente."
                        : "Salida registrada correctamente.");
            });

            task.setOnFailed(event -> {
                Throwable error = task.getException();

                showError(error instanceof Exception e
                        ? e
                        : new RuntimeException(error));
            });

            appExecutor.execute(task);
        });
    }

    private void registerEquipment(InventoryItem item) {

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Registrar equipos");
        dialog.setHeaderText(item.name());

        TextArea txtSerials = new TextArea();
        txtSerials.setPromptText(
                "Ingresa un número de serie por línea:\n" +
                        "TPL-C6-0001\n" +
                        "TPL-C6-0002\n" +
                        "TPL-C6-0003"
        );

        txtSerials.setPrefRowCount(8);
        txtSerials.setWrapText(false);
        txtSerials.getStyleClass().add("secondary-input");

        Label lblInfo = new Label(
                "Puedes registrar varios equipos a la vez. " +
                        "Cada línea representa un equipo físico."
        );
        lblInfo.setWrapText(true);
        lblInfo.getStyleClass().add("secondary-text");

        VBox content = new VBox(12, lblInfo, txtSerials);
        content.setPrefWidth(450);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL,
                ButtonType.OK
        );

        dialog.setResultConverter(button -> {

            if (button != ButtonType.OK) {
                return null;
            }

            return txtSerials.getText()
                    .lines()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        });

        dialog.showAndWait().ifPresent(serials -> {
            try {
                UUID staffUuid = UUID.fromString(getStaffUseCase.getCurrentStaff().getUuid());
                Task<Integer> task = new Task<>() {
                    @Override
                    protected Integer call() {
                        return createInventoryUseCase.registerEquipments(
                                item.uuid(),
                                txtSerials.getText(),
                                staffUuid
                        );
                    }
                };
                executeTask(task, registered -> {
                    refreshInventory();
                    showMessage("Se registraron " + registered + " equipos correctamente.");
                });
            } catch (Exception e) {
                showError(e);
            }
        });
    }

    @FXML
    private void registerEntry() {
        selectConsumable(true);
    }

    @FXML
    private void registerExit() {
        selectConsumable(false);
    }

    private void selectConsumable(boolean entry) {

        List<InventoryItem> consumables = items;

        if (consumables.isEmpty()) {
            showMessage("No hay materiales consumibles registrados.");
            return;
        }

        Dialog<InventoryItem> dialog = new Dialog<>();

        dialog.setTitle("Seleccionar artículo");
        dialog.setHeaderText(
                entry ? "Registrar entrada" : "Registrar salida"
        );

        ButtonType btnAccept = new ButtonType(
                "Aceptar",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                btnAccept,
                ButtonType.CANCEL
        );

        ComboBox<InventoryItem> comboBox = new ComboBox<>();

        comboBox.getItems().addAll(consumables);
        comboBox.getSelectionModel().selectFirst();
        comboBox.setPrefWidth(350);
        comboBox.setMaxWidth(Double.MAX_VALUE);

        comboBox.setConverter(new StringConverter<>() {

            @Override
            public String toString(InventoryItem item) {
                if (item == null) return "";

                return item.name() + " (" + item.code() + ")";
            }

            @Override
            public InventoryItem fromString(String value) {
                return null;
            }
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Selecciona un material:"),
                comboBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(420);

        dialog.setResultConverter(button ->
                button == btnAccept
                        ? comboBox.getValue()
                        : null
        );

        dialog.showAndWait().ifPresent(item ->
                registerMovement(item, entry)
        );
    }

    @FXML
    private void showMovements() {
        Task<List<InventoryMovement>> task = new Task<>() {
            @Override
            protected List<InventoryMovement> call() {
                return getInventoryUseCase.getMovements(null);
            }
        };
        executeTask(task, movements -> {
            TextArea area = new TextArea();
            area.setEditable(false);
            area.setWrapText(true);
            StringBuilder text = new StringBuilder();
            for (InventoryMovement movement : movements) {
                text.append(movement.createdAt())
                        .append(" | ").append(movement.movementType())
                        .append(" | ").append(movement.itemName())
                        .append(" | ").append(movement.quantity())
                        .append(" | Stock: ").append(movement.previousStock())
                        .append(" → ").append(movement.newStock()).append("\n");
            }
            area.setText(text.toString());
            area.setPrefSize(700, 450);
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Historial de movimientos");
            dialog.getDialogPane().setContent(area);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        });
    }

    private void showError(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Inventario");
        alert.setHeaderText("No se pudo completar la operación");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventario");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
