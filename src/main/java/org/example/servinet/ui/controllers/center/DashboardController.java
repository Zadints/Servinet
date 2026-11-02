package org.example.servinet.ui.controllers.center;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.scene.layout.HBox;

import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.ui.navigator.Navigator;

public class DashboardController {

    private boolean puedeVerGrafico;
    private boolean puedeVerTops;
    private boolean mostrandoGrafico;

    @FXML private VBox incomeView;
    @FXML private VBox techniciansView;
    @FXML private VBox sellersView;
    @FXML private Button btnAlternate;
    @FXML private HBox topCards;


    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final 

    public DashboardController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
    }

    @FXML public void initialize() {

    }


    @FXML
    public void alternateOptions() {

        // Solo alternamos si tiene ambos permisos
        if (!puedeVerGrafico || !puedeVerTops) {
            return;
        }

        // Cambiar entre gráfico y rankings
        mostrandoGrafico = !mostrandoGrafico;

        // Actualizar los controles
        actualizarVistaDashboard();
    }


    private void actualizarVistaDashboard() {

        // GRÁFICO DE INGRESOS
        boolean mostrarGrafico =
                puedeVerGrafico && mostrandoGrafico;

        mostrar(incomeView, mostrarGrafico);

        // RANKING DE TÉCNICOS Y VENDEDORES
        boolean mostrarRankings =
                puedeVerTops && !mostrandoGrafico;

        mostrar(techniciansView, mostrarRankings);
        mostrar(sellersView, mostrarRankings);

        // ACTUALIZAR TEXTO DEL BOTÓN
        if (puedeVerGrafico && puedeVerTops) {

            btnAlternate.setText(
                    mostrandoGrafico
                            ? "Ver tops"
                            : "Ver gráfico"
            );
        }
    }

    private void mostrar(Node componente, boolean permitido) {

        componente.setVisible(permitido);
        componente.setManaged(permitido);*/
    }

    public void alternateOptions(ActionEvent actionEvent) {
    }
}
