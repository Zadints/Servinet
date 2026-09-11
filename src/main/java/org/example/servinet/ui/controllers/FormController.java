package org.example.servinet.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.servinet.core.domain.enums.FormType;

public class FormController {


    private StackPane parent;
    @FXML
    private TextField txtUserName;
    @FXML
    private TextField txtUserEmail;
    @FXML
    private ComboBox cbxUserRole;
    @FXML
    private PasswordField txtUserPassword;
    @FXML
    private ComboBox<String> cbxAppTheme;

    @FXML
    private CheckBox chkAutoStart;

    @FXML
    private CheckBox chkNotifications;

    @FXML
    private PasswordField txtCurrentPassword;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

//======================================================================
    @FXML
    private GridPane menuUsuario;

    @FXML
    private GridPane menuUserEdit;

    @FXML
    private GridPane menuAppRename;

    @FXML
    private GridPane menuAppChangeImage;

    @FXML
    private GridPane menuAppSecurity;

    @FXML
    private GridPane menuAppSetting;

    @FXML
    private GridPane menuAppSectorAntenna;

    @FXML
    private GridPane menuAppRolPermission;

    @FXML
    private GridPane menuAnnounceCreate;

    @FXML
    private GridPane menuAntena;

    @FXML
    private GridPane menuAntennaCreate;

    @FXML
    private GridPane menuAntennaEdit;

    @FXML
    private GridPane menuAntennaStartMant;

    @FXML
    private GridPane menuBackuptCreate;

    public void setParent(StackPane parent, FormType type) {

        this.parent = parent;

        // Ocultar todos los formularios
        menuUsuario.setVisible(false);
        menuUsuario.setManaged(false);

        menuUserEdit.setVisible(false);
        menuUserEdit.setManaged(false);

        menuAppRename.setVisible(false);
        menuAppRename.setManaged(false);

        menuAppChangeImage.setVisible(false);
        menuAppChangeImage.setManaged(false);

        menuAppSecurity.setVisible(false);
        menuAppSecurity.setManaged(false);

        menuAppSetting.setVisible(false);
        menuAppSetting.setManaged(false);

        menuAppSectorAntenna.setVisible(false);
        menuAppSectorAntenna.setManaged(false);

        menuAppRolPermission.setVisible(false);
        menuAppRolPermission.setManaged(false);

        menuAnnounceCreate.setVisible(false);
        menuAnnounceCreate.setManaged(false);

        menuAntennaCreate.setVisible(false);
        menuAntennaCreate.setManaged(false);

        menuAntena.setVisible(false);
        menuAntena.setManaged(false);

        menuAntennaEdit.setVisible(false);
        menuAntennaEdit.setManaged(false);

        menuAntennaStartMant.setVisible(false);
        menuAntennaStartMant.setManaged(false);

        menuBackuptCreate.setVisible(false);
        menuBackuptCreate.setManaged(false);


        switch (type) {

            case ANTENNA -> {

                menuAntena.setVisible(true);
                menuAntena.setManaged(true);

                menuAntena.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );

                menuAntena.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.4)
                );
            }

            case USER_CREATE -> {

                menuUsuario.setVisible(true);
                menuUsuario.setManaged(true);

                menuUsuario.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUsuario.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUsuario.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case USER_EDIT -> {

                menuUserEdit.setVisible(true);
                menuUserEdit.setManaged(true);

                menuUserEdit.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUserEdit.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );

                menuUserEdit.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case APP_RENAME -> {

                menuAppRename.setVisible(true);
                menuAppRename.setManaged(true);

                menuAppRename.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppRename.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppRename.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppRename.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.3)
                );
            }

            case APP_CHANGE_IMAGE -> {

                menuAppChangeImage.setVisible(true);
                menuAppChangeImage.setManaged(true);

                menuAppChangeImage.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppChangeImage.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppChangeImage.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.3)
                );

                menuAppChangeImage.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.3)
                );
            }

            case APP_SECURITY -> {

                menuAppSecurity.setVisible(true);
                menuAppSecurity.setManaged(true);

                menuAppSecurity.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case APP_SETTING -> {

                menuAppSetting.setVisible(true);
                menuAppSetting.setManaged(true);

                menuAppSetting.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case APP_SECTOR_ANTENNA -> {

                menuAppSectorAntenna.setVisible(true);
                menuAppSectorAntenna.setManaged(true);

                menuAppSectorAntenna.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.4)
                );

                menuAppSectorAntenna.minWidthProperty().bind(
                        parent.widthProperty().multiply(0.4)
                );

                menuAppSectorAntenna.maxWidthProperty().bind(
                        parent.widthProperty().multiply(0.4)
                );

                menuAppSectorAntenna.prefHeightProperty().bind(
                        parent.heightProperty().multiply(0.3)
                );
            }

            case APP_ROL_PERMISSION -> {

                menuAppRolPermission.setVisible(true);
                menuAppRolPermission.setManaged(true);

                menuAppRolPermission.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }

            case ANNOUNCE_CREATE -> {

                menuAnnounceCreate.setVisible(true);
                menuAnnounceCreate.setManaged(true);

                menuAnnounceCreate.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }

            case ANTENNA_CREATE -> {

                menuAntennaCreate.setVisible(true);
                menuAntennaCreate.setManaged(true);

                menuAntennaCreate.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }

            case ANTENNA_EDIT -> {

                menuAntennaEdit.setVisible(true);
                menuAntennaEdit.setManaged(true);

                menuAntennaEdit.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }

            case ANTENNA_START_MANT -> {

                menuAntennaStartMant.setVisible(true);
                menuAntennaStartMant.setManaged(true);

                menuAntennaStartMant.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.5)
                );
            }

            case BACKUPT_CREATE -> {

                menuBackuptCreate.setVisible(true);
                menuBackuptCreate.setManaged(true);

                menuBackuptCreate.prefWidthProperty().bind(
                        parent.widthProperty().multiply(0.6)
                );
            }
        }
    }





    public void selectUserImage(){

    }
    public void createUser(){
        String newUserName = txtUserName.getText();
        String newUserEmail = txtUserEmail.getText();
        String newUserPassword = txtUserPassword.getText();
    }
    public void selectAntennaImage(){

    }
    public void createAntenna(){

    }
    public void editUser(){

    }
    public void renameApp(){

    }
    public void changeAppImage(){

    }
    public void selectAppImage(){

    }
    public void changeAppSecurity(){

    }
    public void saveAppSettings(){

    }
    public void createAnnounce(){

    }
    public void selectAnnounceImage(){

    }
    public void saveRolePermissions(){

    }
    public void deleteRole(){

    }
    public void createRole(){

    }
    public void saveSector(){

    }
    public void deleteSector(){

    }
    public void createSector(){

    }
    public void selectEditAntennaImage(){

    }
    public void editAntenna(){

    }
    public void saveMaintenance(){

    }
    public void createBackup(){

    }


    public void closeForm(){
        parent.getChildren().clear();
        parent.setVisible(false);
    }
}
