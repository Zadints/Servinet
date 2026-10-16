package org.example.servinet.core.application.startapp;

public class ejemplos{

}

/*

  - para usar Java currency importa:
        import org.example.servinet.infrastructure.concurrency.AppExecutor;

   esa clase te permitirá usar 4 hilos de forma automática de la siguiente forma


public class Ejemplos {


    @FXML
    private Label lblNombre;

    @FXML
    private Label lblCorreo;

    public void cargarUsuario() {

        Task<User> task = new Task<>() {
            // ⚠️ NO necesariamente "un nuevo hilo" que empeiza a ejecutar automático
            // esto es crear un nuevo objeto task que más abajo llamarás
            // (AppExecutor asigna esta Task a uno de sus Worker Threads)
            @Override
            protected User call() {
                return UserModel.getUserDatabase("Augusto");
            }

            //ojo si quieres que ejecute sin retornar nada usa Void con V mayúscula en lugar de
            //la entidad User.
        };

        task.setOnRunning(event -> {
            // ejecuta en el hilo javafx
            // 🖥️ JavaFX Application Thread
            System.out.println("Cargando usuario...");
        });

        task.setOnSucceeded(event -> {
            // ejecuta en el hilo javafx
            // 🖥️ JavaFX Application Thread
            User user = task.getValue();


            lblNombre.setText(user.getName());
            lblCorreo.setText(user.getEmail());
        });

        task.setOnFailed(event -> {
            // ejecuta en el hilo javafx
            // 🖥️ JavaFX Application Thread
            System.out.println("Error:");
            task.getException().printStackTrace();
        });

        task.setOnCancelled(event -> {
            // ejecuta en el hilo javafx
            // 🖥️ JavaFX Application Thread
            System.out.println("Consulta cancelada");
        });

        AppExecutor.submit(task); //Esto ejecuta el objeto creado en un hilo distinto al de Javafx
    }


}



package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Plan;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class PlanModel {

    public static List<Plan> getAllPlansDatabase() {
        String sql = "SELECT plan_id, plan_name, plan_speed, plan_price, plan_ispromo FROM plans";
        Connection conn = LoadDb.getConnection();
        List<Plan> plans = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                plans.add(new Plan(
                        rs.getString("plan_id"),
                        rs.getString("plan_name"),
                        rs.getInt("plan_speed"),
                        rs.getDouble("plan_price"),
                        rs.getBoolean("plan_ispromo")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron obtener los planes", e);
        }
        return plans;
    }

    public static Plan getPlanByIdDatabase(String planId) {
        String sql = "SELECT plan_id, plan_name, plan_speed, plan_price, plan_ispromo FROM plans WHERE plan_id = ?";
        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Plan(
                        rs.getString("plan_id"),
                        rs.getString("plan_name"),
                        rs.getInt("plan_speed"),
                        rs.getDouble("plan_price"),
                        rs.getBoolean("plan_ispromo")
                );
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el plan", e);
        }
        return null;
    }

    public static void createPlanDatabase(Plan plan) {
        String sql = "INSERT INTO plans (plan_id, plan_name, plan_speed, plan_price, plan_ispromo) VALUES (?, ?, ?, ?, ?)";
        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, plan.getPlanId());
            stmt.setString(2, plan.getPlanName());
            stmt.setInt(3, plan.getPlanSpeed());
            stmt.setDouble(4, plan.getPlanPrice());
            stmt.setBoolean(5, plan.isPlanIsPromo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo crear el plan", e);
        }
    }

    public static void updatePlanDatabase(Plan plan) {
        String sql = "UPDATE plans SET plan_name = ?, plan_speed = ?, plan_price = ?, plan_ispromo = ? WHERE plan_id = ?";
        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, plan.getPlanName());
            stmt.setInt(2, plan.getPlanSpeed());
            stmt.setDouble(3, plan.getPlanPrice());
            stmt.setBoolean(4, plan.isPlanIsPromo());
            stmt.setString(5, plan.getPlanId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo actualizar el plan", e);
        }
    }

    public static void deletePlanDatabase(String planId) {
        String sql = "DELETE FROM plans WHERE plan_id = ?";
        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, planId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el plan", e);
        }
    }
}
*/
