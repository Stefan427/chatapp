package com.example.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import static javafx.scene.paint.Color.TRANSPARENT;


public class UserList {
    @FXML
    private ImageView homeImageView;
    @FXML
    private Button firstPersonButton; // stefan
    @FXML
    private Button secondPersonButton; // Ron
    @FXML
    private Button thirdPersonButton;// mohammad
    @FXML
    private Button fourthPersonButton;

    private HashMap<String,Integer> usernameList = new HashMap<>();

    public void btnCustomize(String username) throws IOException {
        // customize the button content for usernames that are in contactList
        Platform.runLater(() -> {
            if (username.equals("stefan") || username.equals("ron")||username.equals("mohammad")) {
                Button fourthpersonButton = new Button();
                fourthpersonButton.setText("other");
                fourthpersonButton.setPrefWidth(180);
                fourthpersonButton.setPrefHeight(40);
                fourthpersonButton.setLayoutX(169);
                fourthpersonButton.setLayoutY(312);
            }
            switch (username) {
                case "stefan":
                    firstPersonButton.setText("Digital Disorder");
                    break;
                case "ron":
                    secondPersonButton.setText("Digital Disorder");
                    break;
                case "mohammad":
                    thirdPersonButton.setText("Digital Disorder");
                    break;
            }
        });

    }
    @FXML
    protected void onHomeClicked() {
        // This method is called when the home icon is clicked
        try {
            // Load the first scene (hello-view.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene firstScene = new Scene(fxmlLoader.load(), 620, 440);

            String css = getClass().getResource("homePage.css").toExternalForm();
            firstScene.getStylesheets().add(css);
            firstScene.setFill(TRANSPARENT);
            // Get the current stage (window) and set the first scene
            Stage stage = (Stage) homeImageView.getScene().getWindow();
            // to make the page moveable
            AtomicReference<Double> offsetX = new AtomicReference<>((double) 0);
            AtomicReference<Double> offsetY = new AtomicReference<>((double) 0);
            // to pass it to setOnMousePressed it needs to be a reference
            firstScene.setOnMousePressed(event -> {
                offsetX.set(event.getSceneX());
                offsetY.set(event.getSceneY());
            });
            firstScene.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - offsetX.get());
                stage.setY(event.getScreenY() - offsetY.get());
            });
            stage.setScene(firstScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Print the error if the scene could not be loaded
        }
    }
    @FXML
    public void exitOnClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yesButton) {
            System.exit(0);
        }
    }

    protected Button FourthButtonMaker(String username) {
        Button fourthpersonButton = new Button();
        fourthpersonButton.setText("other");
        fourthpersonButton.setPrefWidth(180);
        fourthpersonButton.setPrefHeight(40);
        fourthpersonButton.setLayoutX(95);
        fourthpersonButton.setLayoutY(327);
        return fourthpersonButton;
    }
    private void BtnClicked(ActionEvent actionEvent) {

    }
}
