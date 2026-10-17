package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.scene.Node;
import javafx.scene.layout.HBox;

import org.example.servinet.core.application.security.PermissionPolicy;
import org.example.servinet.core.domain.enums.Permission;

public class DashboardController {

    private boolean puedeVerGrafico;
    private boolean puedeVerTops;
    private boolean mostrandoGrafico;
    @FXML
    private VBox incomeView;

    @FXML
    private VBox techniciansView;

    @FXML
    private VBox sellersView;
    @FXML
    private Button btnAlternate;

    @FXML
    private HBox topCards;


    @FXML
    public void initialize() {

        // Obtener permisos una sola vez
        PermissionPolicy policy = PermissionPolicy.current();

        // Evaluar permisos de la sesión
        puedeVerGrafico = policy.allows(
                Permission.DASH_VIEW_GRAPH
        );

        puedeVerTops = policy.allows(
                Permission.DASH_VIEW_TOPS
        );

        // Iniciar mostrando el gráfico si está permitido
        mostrandoGrafico = puedeVerGrafico;

        // Mostrar las tarjetas de estadísticas
        mostrar(topCards, puedeVerTops);

        // El botón para alternar solo se muestra
        // cuando tiene permiso para ambas vistas
        mostrar(
                btnAlternate,
                puedeVerGrafico && puedeVerTops
        );

        // Renderizar las secciones autorizadas
        actualizarVistaDashboard();
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
        componente.setManaged(permitido);
    }

}
