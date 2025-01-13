package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextField InputIpField;
    @FXML
    private TextField inputPortField;
    @FXML
    private Button connectBtn;
    @FXML
    private TextField InputUser;

    private Socket socket;
    private PrintWriter out;

    @FXML
    protected void connectOnClick() {
        int port = 12345; // default
        String ipAddress = InputIpField.getText();
        if (!inputPortField.getText().trim().isEmpty()){
            port = Integer.parseInt(inputPortField.getText());
        }
        String username = InputUser.getText().replaceAll("\\s", "");

        try {
            socket = new Socket(ipAddress == null ? "127.0.0.1" : ipAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Load chat room scene
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/chat-room.fxml"));
            Scene firstScene = new Scene(fxmlLoader.load(), 620, 440);

            // Pass the PrintWriter and Socket to ChatRoom controller
            ChatRoom chatRoomController = fxmlLoader.getController();
            chatRoomController.setClientConnection(out, socket, username);

            Stage stage = (Stage) connectBtn.getScene().getWindow();
            stage.setScene(firstScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle connection errors
        }
    }
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
}