package com.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ChatRoom {

    @FXML
    private TextField inputMessageField;
    @FXML
    private ImageView homeImageView;
    @FXML
    private VBox messageHistory;
    @FXML
    private ScrollPane scrollPane;

    private PrintWriter out; // To send messages to the server
    private Socket socket; // To read incoming messages

    public void setClientConnection(PrintWriter out, Socket socket,String username) {
        this.out = out;
        this.socket = socket;
        out.println(username);
        try {
            List<String> lines = Files.readAllLines(Paths.get("Chatlog.txt"));
            for (String line : lines) {
                boolean first = false;
                if (line.startsWith(String.valueOf(socket.getPort()))){
                    String[] parts = line.split("\\|");
                    for (String s : parts){
                        if (first){
                            String[] parts2 = s.split(":");
                            if (username.equals(parts2[0])){
                                addMessageToHistory("me: " + parts2[1]);
                            }else{
                                addMessageToHistory(s);
                            }
                        }
                        first = true;
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    receiveMessage(message); // Handle received messages
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void pressedArrow(MouseEvent mouseEvent) {
        sendMessage();
    }

    @FXML
    protected void pressedEnterSendMessage(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            sendMessage();
        }
    }

    private void sendMessage() {
        String message = inputMessageField.getText();
        if (!message.isEmpty()) {
            out.println(message); // Send message to the server
            addMessageToHistory("Me: " + message);  // Only add for sender (Me: prefix)
            inputMessageField.clear();
        }
    }


    public void receiveMessage(String message) {
        // Use Platform.runLater to update the UI
        Platform.runLater(() -> {
            // Split the incoming message to determine the sender
            String[] parts = message.split(": ", 2);
            if (parts.length == 2) {
                String sender = parts[0]; // Extract sender's name
                String receivedMessage = parts[1]; // Extract actual message

                // Display received message only for other clients
                if (!sender.equals("Client" + socket.getPort())) { // Change to check against client's unique identifier
                    addMessageToHistory(sender + ": " + receivedMessage);
                }
            } else {
                // Fallback if format is incorrect
                addMessageToHistory("Unknown: " + message);
            }
        });
    }




    private void addMessageToHistory(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-font-family: Arial;");
        messageHistory.getChildren().add(messageLabel);

        // Auto-scroll to the bottom of the scroll pane
        scrollPane.setVvalue(1.0);
    }

    @FXML
    protected void onHomeClicked() {
        // This method is called when the home icon is clicked
        try {
            // Load the first scene (hello-view.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene firstScene = new Scene(fxmlLoader.load(), 620, 440);

            // Get the current stage (window) and set the first scene
            Stage stage = (Stage) homeImageView.getScene().getWindow();
            stage.setScene(firstScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Print the error if the scene could not be loaded
        }
    }
}