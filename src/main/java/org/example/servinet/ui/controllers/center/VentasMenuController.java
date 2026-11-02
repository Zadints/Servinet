package org.example.servinet.ui.controllers.center;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.application.usecase.clients.GetClientUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;

import javafx.scene.layout.VBox;

import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.ui.controllers.components.OfertasCardController;
import org.example.servinet.ui.controllers.components.SellClientController;
import org.example.servinet.ui.controllers.forms.VentasFormController;
import org.example.servinet.ui.navigator.Navigator;

public class VentasMenuController {

    private final Navigator navigator;
    private final AuthorizationSecurity authorizationSecurity;
    public TextField txtSearchClient;
    public Label lblCoincidence;
    public Button btnClearSearch;
    public ComboBox<String> cmbSearchType;
    @FXML private VBox recentContractsList;
    @FXML private Button btnNewContract;
    private final GetClientUseCase getClientUseCase;
    private final PlanUseCase planUseCase;
    private final AppExecutor appExecutor;
    @FXML private VBox offersList;
    private final AtomicLong searchVersion = new AtomicLong();
    private PauseTransition renderPause;
    private final PauseTransition searchDelay =
            new PauseTransition(Duration.millis(350));

    public VentasMenuController(Navigator navigator, AuthorizationSecurity authorizationSecurity, GetClientUseCase getClientUseCase, PlanUseCase planUseCase, AppExecutor appExecutor) {
        this.navigator = navigator;
        this.authorizationSecurity = authorizationSecurity;
        this.getClientUseCase = getClientUseCase;
        this.planUseCase = planUseCase;
        this.appExecutor = appExecutor;
    }


    @FXML public void initialize() {
        cmbSearchType.getSelectionModel().select("Nombre");

        // Esperar 350 ms después de escribir para buscar
        searchDelay.setOnFinished(event -> searchClients());

        // Buscar automáticamente al escribir
        txtSearchClient.textProperty().addListener(
                (obs, oldValue, newValue) -> searchDelay.playFromStart()
        );

        // Buscar automáticamente al cambiar el tipo
        cmbSearchType.valueProperty().addListener(
                (obs, oldValue, newValue) -> searchDelay.playFromStart()
        );

        reloadOffers();
        loadClients();
    }



    @FXML
    protected void newContract() {


        if (!authorizationSecurity.hasPermission(
                Permission.SELL_CREATE_CONTRATE)) {

            new Alert(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para crear contratos."
            ).showAndWait();

            return;
        }


        try {
            navigator.openWindow(
                    "/org/example/servinet/forms/sell-form.fxml",
                    (Stage) btnNewContract.getScene().getWindow(),
                    0.85,
                    0.85
            );
        }catch (Exception e){
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            root.printStackTrace();
            System.out.println(e.getMessage()+ e.getCause());
        }
    }

    @FXML
    protected void showSelling() {
        if (!authorizationSecurity.hasPermission(
                Permission.SELL_RENEW_CONTRATE)) {

            new Alert(
                    Alert.AlertType.WARNING,
                    "No tienes permiso para renovar contratos."
            ).showAndWait();

            return;
        }


        try {
            navigator.openWindow(
                    "/org/example/servinet/forms/pay-form.fxml",
                    (Stage) btnNewContract.getScene().getWindow(),
                    0.85,
                    0.85
            );
        }catch (Exception e){
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            root.printStackTrace();
            System.out.println(e.getMessage()+ e.getCause());
        }
    }

    @FXML
    protected void reloadOffers() {
        Task<List<ClientPlan>> task = new Task<List<ClientPlan>>() {
            @Override
            protected List<ClientPlan> call() throws Exception {
                return planUseCase.getAll();
            }
        };
        task.setOnSucceeded(event -> {
            List<ClientPlan> plans = task.getValue();
            offersList.getChildren().clear();
            if (plans == null || plans.isEmpty()){
                Label lb = new Label();
                lb.setText("No se encontraron ofertas");
                offersList.getChildren().add(lb);
                return;
            }
            loadViewPlans(plans);

        });
        appExecutor.execute(task);
    }
    private void loadClients(){
        Task<List<Client>> task2 = new Task<List<Client>>() {
            @Override
            protected List<Client> call() throws Exception {
                return getClientUseCase.getAll();
            }
        };
        task2.setOnSucceeded(event -> {
            List<Client> clients = task2.getValue();
            recentContractsList.getChildren().clear();
            if (clients == null || clients.isEmpty()){
                Label lb = new Label();
                lb.setText("No se encontraron clients");
                recentContractsList.getChildren().add(lb);
                return;
            }
            for (Client c : clients){
                if (c.getPlanStatus().equals(PlanStatusClient.CON_DEUDA)) {
                    loadViewClients(c);
                }
            }


        });
        appExecutor.execute(task2);
    }



    private void loadViewPlans(List<ClientPlan> plans){
        for (ClientPlan p : plans){

            try {
                OfertasCardController of = navigator.loadComponent(
                        "/org/example/servinet/components/ofertas-card.fxml",
                        offersList.getChildren()::add
                );

                of.setPlanOfert(p);
                of.selectClientPlan( selectedPlan -> {
                    try {
                        navigator.openWindow(
                                "/org/example/servinet/forms/sell-form.fxml",
                                (Stage) btnNewContract.getScene().getWindow(),
                                0.80,
                                0.80,
                                controller -> {
                                    VentasFormController ventas= (VentasFormController) controller;
                                    ventas.setPlanOptional(selectedPlan);
                                    }
                        );

                    }catch (Exception e){
                        Throwable root = e;
                        while (root.getCause() != null) root = root.getCause();
                        root.printStackTrace();
                        System.out.println(e.getMessage()+ e.getCause());
                    }
                });
            }catch (Exception e){
                System.out.println(e.getMessage()+ e.getCause());
            }



        }
    }

    private void loadViewClients(Client c){

        try {
            SellClientController of = navigator.loadComponent(
                    "/org/example/servinet/components/sell-clients.fxml",
                    recentContractsList.getChildren()::add
            );

            of.setClient(c);
        }catch (Exception e){
            System.out.println(e.getMessage()+ e.getCause());
        }
    }

    public void clearSearch(ActionEvent actionEvent) {

        txtSearchClient.clear();

        loadClients();
    }

    public void searchClients() {

        String type = cmbSearchType.getValue();
        String query = txtSearchClient.getText().trim();

        if (type == null || type.isBlank()) {
            return;
        }

        long currentVersion = searchVersion.incrementAndGet();

        Task<List<Client>> task = new Task<>() {

            @Override
            protected List<Client> call() throws Exception {
                return getClientUseCase.searchClients(type, query);
            }
        };

        task.setOnSucceeded(event -> {

            if (currentVersion != searchVersion.get()) return;

            List<Client> clients = task.getValue();

            recentContractsList.getChildren().clear();

            lblCoincidence.setText(
                    clients.size() + " Coincidencias"
            );

            for (Client client : clients) {
                loadViewClients(client);
            }
        });

        task.setOnFailed(event -> {

            if (currentVersion != searchVersion.get()) return;

            Throwable error = task.getException();

            System.err.println(
                    "Error buscando clientes: " + error.getMessage()
            );
        });

        appExecutor.execute(task);
    }
}
