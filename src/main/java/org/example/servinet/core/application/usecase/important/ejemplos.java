package org.example.servinet.core.application.usecase.important;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.infrastructure.database.models.UserModel;

public class ejemplos{

}

/*
public class Ejemplos {

    @FXML
    private Label lblNombre;

    @FXML
    private Label lblCorreo;

    public void cargarUsuario() {

        Task<User> task = new Task<>() {
            // ⚠️ NO necesariamente "un nuevo hilo"
            // AppExecutor asigna esta Task a uno de sus Worker Threads
            @Override
            protected User call() {
                return UserModel.getUserDatabase("Augusto");
            }
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

        AppExecutor.submit(task);
    }


}
*/

