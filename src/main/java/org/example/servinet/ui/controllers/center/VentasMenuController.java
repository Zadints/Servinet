package org.example.servinet.ui.controllers.center;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;

public class VentasMenuController {
    @FXML
    public void initialize() {
        cargarMetricasVentas();
    }
    private void cargarMetricasVentas() {
        String urlBD = "jdbc:sqlserver://localhost:1433;databaseName=servinet;integratedSecurity=true;encrypt=false;";

        // Consultas basadas en tus tablas `payments` y `orders`
        String sqlMes = "SELECT COUNT(p.payment_id) AS total_ventas, ISNULL(SUM(p.payment_amount), 0) AS monto_total " +
                "FROM payments p " +
                "WHERE MONTH(p.payment_date) = MONTH(GETDATE()) " +
                "  AND YEAR(p.payment_date) = YEAR(GETDATE())";

        String sqlHoy = "SELECT COUNT(payment_id) FROM payments WHERE CAST(payment_date AS DATE) = CAST(GETDATE() AS DATE)";

        try (Connection conn = DriverManager.getConnection(urlBD)) {
            // 1. Datos del Mes
            try (PreparedStatement stmt = conn.prepareStatement(sqlMes);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalMes = rs.getInt("total_ventas");
                    double montoTotal = rs.getDouble("monto_total");

                    if (lblVentasMes != null) {
                        lblVentasMes.setText(String.valueOf(totalMes));
                    }

                    // Estimación de comisión (ejemplo: 10% del total vendido)
                    if (lblComision != null) {
                        lblComision.setText(String.format("S/ %.2f", montoTotal * 0.10));
                    }

                    // Progreso del objetivo mensual (ejemplo basado en una meta de 20 ventas)
                    int metaMensual = 20;
                    int progreso = (int) (((double) totalMes / metaMensual) * 100);
                    if (lblObjetivoMensual != null) {
                        lblObjetivoMensual.setText(progreso + " %");
                    }
                }
            }

            // 2. Datos de Hoy
            try (PreparedStatement stmt = conn.prepareStatement(sqlHoy);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalHoy = rs.getInt(1);
                    if (lblVentasHoy != null) {
                        lblVentasHoy.setText(String.valueOf(totalHoy));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private IndexController indexController;
    @FXML private Label lblVentasMes;
    @FXML private Label lblVentasHoy;
    @FXML private Label lblComision;
    @FXML private Label lblObjetivoMensual;
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    protected void newContract(){
        indexController.renderizarFxml("sell-form.fxml");
    }
    @FXML
    protected void showSelling() {
        if (indexController != null) {
            indexController.renderizarFxml("registrar-pago.fxml"); // O la vista correspondiente a pagos
        }
    }
    @FXML
    protected void showOffers() {
        if (indexController != null) {
            indexController.renderizarFxml("ofertas.fxml");
        }
    }

    @FXML
    protected void showContracts() {
        if (indexController != null) {
            indexController.renderizarFxml("mis-contratos.fxml");
        }
    }

    @FXML
    protected void selectOffer() {
        if (indexController != null) {
            indexController.renderizarFxml("sell-form.fxml");
        }
    }

    @FXML
    protected void editContract() {
        if (indexController != null) {
            indexController.renderizarFxml("editar-contrato.fxml");
        }
    }

    @FXML
    protected void showStatistics() {
        if (indexController != null) {
            indexController.renderizarFxml("estadisticas-ventas.fxml");
        }
    }

    @FXML
    protected void showProspects() {
        if (indexController != null) {
            indexController.renderizarFxml("prospectos.fxml");
        }
    }

}
