package org.example.servinet.ui.controllers.forms;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.servinet.application.dto.AnnouncementDto;
import org.example.servinet.application.usecase.announce.CreateAnnounceUseCase;
import org.example.servinet.application.usecase.announce.EditAnnounceUseCase;
import org.example.servinet.application.usecase.announce.GetAnnounceUseCase;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.ui.events.MouseMove;


import java.io.File;
import java.nio.file.Path;

public class AnnounceCreateController {

    @FXML private Button btnClose;
    @FXML private GridPane menuAnnounceCreate;
    @FXML private Label lblAnnounceFormTitle;
    @FXML private TextField txtAnnounceTitle;
    @FXML private ComboBox<String> cbxAnnouncePriority;
    @FXML private TextArea txtAnnounceContent;
    @FXML private Button btnSelectAnnounceImage;
    @FXML private Label lblAnnounceTitleCount;
    @FXML private Label lblAnnounceContentCount;
    @FXML private Label lblAnnounceError;
    @FXML private Button btnAnnounceSubmit;
    private Announcement announcement;
    private Path image;

    private final CreateAnnounceUseCase createAnnounceUseCase;
    private final EditAnnounceUseCase editAnnouncement;
    private final GetAnnounceUseCase getAnnounceUseCase;

    public AnnounceCreateController(CreateAnnounceUseCase createAnnounceUseCase, EditAnnounceUseCase editAnnouncement, GetAnnounceUseCase getAnnounceUseCase) {
        this.createAnnounceUseCase = createAnnounceUseCase;
        this.editAnnouncement = editAnnouncement;
        this.getAnnounceUseCase = getAnnounceUseCase;
    }

    public void initialize(){
        MouseMove mouseMove = new MouseMove(menuAnnounceCreate);
        mouseMove.start();
    }


    private void setupAnnounceForm() {

        txtAnnounceTitle.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= getAnnounceUseCase.getTitleMax() ? change : null));
        txtAnnounceContent.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= getAnnounceUseCase.getDescriptionMax() ? change : null));

        txtAnnounceTitle.textProperty().addListener((obs, oldValue, newValue) ->
                updateCounter(lblAnnounceTitleCount, newValue, getAnnounceUseCase.getTitleMax()));
        txtAnnounceContent.textProperty().addListener((obs, oldValue, newValue) ->
                updateCounter(lblAnnounceContentCount, newValue, getAnnounceUseCase.getDescriptionMax()));

        updateCounter(lblAnnounceTitleCount, "", getAnnounceUseCase.getTitleMax());
        updateCounter(lblAnnounceContentCount, "", getAnnounceUseCase.getDescriptionMax());

        cbxAnnouncePriority.setItems(FXCollections.observableArrayList(getAnnounceUseCase.getPrioritiesMax()));

        if (announcement != null) {
            lblAnnounceFormTitle.setText("Editar anuncio");
            btnAnnounceSubmit.setText("Guardar cambios");
            txtAnnounceTitle.setText(announcement.getTitle());
            txtAnnounceContent.setText(announcement.getDescription());
            cbxAnnouncePriority.setValue(announcement.getPriority());
            btnSelectAnnounceImage.setText("Cambiar imagen (opcional)");
        } else {
            cbxAnnouncePriority.setValue("Media");
        }
    }

    /**
     *  Solo llamar cuando vas a editar
     **/
    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
        setupAnnounceForm();
    }

    public void selectAnnounceImage(){

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen del anuncio");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.png", "*.bmp"));

        File file = fileChooser.showOpenDialog(btnSelectAnnounceImage.getScene().getWindow());
        if (file != null) {
            image = file.toPath();
            btnSelectAnnounceImage.setText(file.getName());
        }
    }

    public void saveAnnounce(){

        AnnouncementDto dto = new AnnouncementDto(
                txtAnnounceTitle.getText(),
                txtAnnounceContent.getText(),
                cbxAnnouncePriority.getValue(),
                image
        );

        try {
            if (announcement != null) {
                editAnnouncement.editAnnouncement(announcement, dto);
            } else {
                createAnnounceUseCase.createAnnouncement(dto);
            }

            closeForm();

        } catch (RuntimeException e) {
            lblAnnounceError.setText(e.getMessage());
            lblAnnounceError.setVisible(true);
            lblAnnounceError.setManaged(true);
        }
    }

    public void closeForm() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private void updateCounter(Label label, String text, int max) {
        int length = text == null ? 0 : text.length();
        label.setText(length + "/" + max);
        label.setStyle(length >= max ? "-fx-text-fill: #E5484D; -fx-font-weight: bold;" : "");
    }
}
