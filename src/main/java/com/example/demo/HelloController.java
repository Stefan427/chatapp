package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.paint.Color.TRANSPARENT;

public class HelloController {
    private Thread serverThread;
    int port = 12345; // default

    private Set<String> users =  Set.of("stefan", "ron", "mohammad");

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
    @FXML

    private Socket socket;
    private PrintWriter out;

    @FXML
    protected void connectOnClick() {

        String ipAddress = InputIpField.getText();

        if (!inputPortField.getText().trim().isEmpty()){
            port = Integer.parseInt(inputPortField.getText());
        }
        String username = "default" + (int) (Math.random()*1000);
                if(!InputUser.getText().trim().isEmpty()){
                    username = InputUser.getText().replaceAll("\\s", "");
                }

        if (isPortInUse(port)) {
            System.out.println("Server läuft bereits!");

        }else{
        serverThread = new Thread(new ChatServer(port));
        serverThread.setDaemon(true);
        serverThread.start();}
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();}
        try {
            socket = new Socket(ipAddress == null ? "127.0.0.1" : ipAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Load chat room scene
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/chat-room.fxml"));
            Scene firstScene = new Scene(fxmlLoader.load(), 620, 440);
            String css = getClass().getResource("chatRoom.css").toExternalForm();
            firstScene.getStylesheets().add(css);
            firstScene.setFill(TRANSPARENT);
            // Pass the PrintWriter and Socket to ChatRoom controller
            ChatRoom chatRoomController = fxmlLoader.getController();
            chatRoomController.setClientConnection(out, socket, username);

            Stage stage = (Stage) connectBtn.getScene().getWindow();
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
        } catch (Exception e1) {
            System.out.println(e1);; // Handle connection errors
        }
    }
    protected String getIpAddress() {
        return InputIpField.getText();
    }

    boolean isPortInUse(int port) {
        try (ServerSocket tempSocket = new ServerSocket(port)) {
            return false; // Port ist frei
        } catch (Exception e) {
            return true; // Port ist belegt
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
    @FXML
    public void contactListOnClick() throws IOException {
        String username = "default" + (int) (Math.random()*1000);
        if(!InputUser.getText().trim().isEmpty()){
            username = InputUser.getText().replaceAll("\\s", "").toLowerCase(Locale.ROOT);
        }
        try {
            // Load the first scene (hello-view.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("user-list.fxml"));
            Scene firstScene = new Scene(fxmlLoader.load(), 620, 440);

            UserList contactList = fxmlLoader.getController();
            contactList.btnCustomize(username);

            String css = getClass().getResource("contactList.css").toExternalForm();
            firstScene.getStylesheets().add(css);
            firstScene.setFill(TRANSPARENT);

            // Get the current stage (window) and set the first scene
            Stage stage = (Stage) connectBtn.getScene().getWindow();
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
    public void saveUsernameOnClick(){
        String username;
        if(!InputUser.getText().trim().isEmpty()){
            username = InputUser.getText().replaceAll("\\s", "");
            List<String> lines = new ArrayList<>();
            boolean found = false;
            // Datei lesen und in den Speicher laden
            try (BufferedReader reader = new BufferedReader(new FileReader("Usernames.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(username)) {
                        found = true;
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Neue Zeile hinzufügen, falls nicht gefunden
            if (!found) {
                lines.add(username);
            }
            // Datei mit aktualisiertem Inhalt überschreiben
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("Usernames.txt"))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}