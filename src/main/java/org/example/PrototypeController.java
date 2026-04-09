package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PrototypeController {
    @FXML
    private Label nameLabel;

    @FXML
    private TextField textField;

    @FXML
    private void handleSubmit(){
        String name = textField.getText();
        nameLabel.setText("Olá " + name);
    }
}