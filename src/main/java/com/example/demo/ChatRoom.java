package com.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.paint.Color.TRANSPARENT;

public class ChatRoom {

    @FXML
    private TextField inputMessageField;
    @FXML
    private ImageView homeImageView;
    @FXML
    private VBox messageHistory;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView contactBtn;
    private HashMap<String,Integer> userContacts = new HashMap<>();

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
                                addMessageToHistory("Me: " + parts2[1]);
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
        String[] parts = message.split(": ", 2);
        String sender = parts[0];

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);

        // Wrap the message label in an HBox for alignment control
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new Insets(5));

        if (sender.equals("Me")) {
            messageLabel.getStyleClass().add("own-message");
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.setPrefWidth(590);
        } else {
            messageLabel.getStyleClass().add("other-message");
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        messageContainer.getChildren().add(messageLabel);
        messageHistory.getChildren().add(messageContainer);

        // Auto-scroll to the bottom
        scrollPane.setVvalue(1.0);
    }
    public void contactListOnClick() throws IOException {

        // just some input for line 139 to check userBtnMaker functionality
        userContacts.put("default1", 1);
        userContacts.put("default2", 12345);


        String username = "default" + (int) (Math.random()*1000);
        try {
            // Load the first scene (hello-view.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("user-list.fxml"));
            Scene firstScene = new Scene(fxmlLoader.load(), 620, 440);

            UserList contactList = fxmlLoader.getController();
            contactList.userBtnMaker(username,userContacts);

            String css = getClass().getResource("contactList.css").toExternalForm();
            firstScene.getStylesheets().add(css);
            firstScene.setFill(TRANSPARENT);

            // Get the current stage (window) and set the first scene
            Stage stage = (Stage) contactBtn.getScene().getWindow();
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
}