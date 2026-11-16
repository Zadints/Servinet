package org.example.servinet.ui.controllers.setup;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class PrivacyPolicyController {
    @FXML private CheckBox chkAcceptPrivacy;

    public boolean isAccepted() {
        return chkAcceptPrivacy.isSelected();
    }
}
