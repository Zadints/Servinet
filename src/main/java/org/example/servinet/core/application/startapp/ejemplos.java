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
*/

