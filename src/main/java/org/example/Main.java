package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Label nameLabel = new Label("Digite seu nome");
        TextField textField = new TextField(("Seu nome aqui"));
        Button submitButton = new Button("Enviar");

        VBox vbox = new VBox(8);
        vbox.getChildren().addAll(nameLabel, textField, submitButton);

        submitButton.setOnAction(e -> {
            String name = textField.getText();
            nameLabel.setText("Olá " + name);
        });

        Scene scene = new Scene(vbox, 300, 200);

        stage.setTitle("Janela Protótipo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
