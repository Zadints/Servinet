package org.example.servinet.ui.controllers.center;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.enums.FormType;
import org.example.servinet.ui.controllers.IndexController;

public class AdministrationController {
    private IndexController indexController;
    @FXML
    private ImageView imgNombreApp;

    @FXML
    public void initialize() {
        //imgNombreApp.setImage(new Image(getClass().getResource("/multimedia/images/Panda.png").toExternalForm()));
        cargarActividad();
    }

    @FXML
    private GridPane activityGrid;

    private void cargarActividad() {

        activityGrid.getChildren().clear();

        int semanas = 30;

        for (int semana = 0; semana < semanas; semana++) {

            for (int dia = 0; dia < 7; dia++) {

                Region actividad = new Region();

                actividad.setMinSize(12, 12);
                actividad.setPrefSize(12, 12);
                actividad.setMaxSize(12, 12);

                int cantidad = (int) (Math.random() * 15);

                int nivel;

                if (cantidad == 0) {
                    nivel = 0;
                } else if (cantidad <= 2) {
                    nivel = 1;
                } else if (cantidad <= 5) {
                    nivel = 2;
                } else if (cantidad <= 10) {
                    nivel = 3;
                } else {
                    nivel = 4;
                }

                actividad.getStyleClass().add(
                        "activity-level-" + nivel
                );

                Tooltip tooltip = new Tooltip(
                        cantidad + (cantidad == 1
                                ? " actividad"
                                : " actividades")
                );

                Tooltip.install(actividad, tooltip);

                // columna = semana
                // fila = día
                activityGrid.add(actividad, semana, dia);
            }
        }
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
    @FXML
    private void userCreate() {
        indexController.abrirModal(FormType.USER_CREATE);

    }
    @FXML
    private void userDelete() {


    }
    @FXML
    private void userEdit() {


    }


    @FXML
    private void appRename(){
        indexController.abrirModal(FormType.APP_RENAME);
    }
    @FXML
    private void appChangeLogo(){
        indexController.abrirModal(FormType.APP_CHANGE_IMAGE);
    }
    @FXML
    private void appSecurity(){
        indexController.abrirModal(FormType.APP_SECURITY);
    }
    @FXML
    private void appSectors(){
        indexController.abrirModal(FormType.APP_SECTOR_ANTENNA);
    }
    @FXML
    private void appRolPermission(){
        indexController.abrirModal(FormType.APP_ROL_PERMISSION);
    }
    @FXML
    private void appConfig(){
        indexController.abrirModal(FormType.APP_SETTING);
    }

}
