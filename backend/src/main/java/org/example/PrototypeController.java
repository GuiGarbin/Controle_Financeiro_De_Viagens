package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PrototypeController {
    @FXML
    private Label nameLabel;

    @FXML
    private TextField textField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleSubmit(){
        String name = textField.getText();
        String pwd = passwordField.getText();
        nameLabel.setText("Olá " + name + ". Pwd: " + pwd);
    }
}