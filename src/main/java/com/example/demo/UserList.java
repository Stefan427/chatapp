package com.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import static javafx.scene.paint.Color.TRANSPARENT;


public class UserList {
    private static int portnumber = 12000;
    private Thread serverThread;
    private Socket socket;
    private PrintWriter out;
    String username;
    @FXML
    private ImageView homeImageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox vBox;
    @FXML
    private TextField createChat;

    HashMap<String,Integer> userContacts;

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

    protected void userBtnMaker(String user, HashMap<String,Integer> usernamesContacts) {
        // this function make a List of contact to chose with whom you want to talk
        username = user;
        userContacts = usernamesContacts;

        Platform.runLater(() -> {
            for(String name : userContacts.keySet()) {
                HBox hBox = new HBox();
                Button newButton = new Button(name);
                newButton.setPrefWidth(250);
                newButton.setPrefHeight(30);

                hBox.getChildren().add(newButton);
                hBox.setSpacing(15);
                hBox.setAlignment(Pos.CENTER);
                int port = userContacts.get(name);
                newButton.setOnAction(event -> {
                    connectOnClick(user , port, (Button) event.getSource());
                });
                vBox.getChildren().add(0, hBox);
                vBox.setSpacing(15);
            }
        });
    }

    protected void connectOnClick(String username, int port, Button buttonClicked) {
        HelloController helloController = new HelloController();
        String ipAddress = "127.0.0.1";

        if (helloController.isPortInUse(port)) {
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

            Stage stage = (Stage) buttonClicked.getScene().getWindow();
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
            System.out.println(e1); // Handle connection errors
        }
    }

    @FXML
    protected void pressedEnterCreateChat(KeyEvent event){
        if (event.getCode().toString().equals("ENTER")) {
            if (!createChat.getText().trim().isEmpty()){
                System.out.println("test1");
                String user = createChat.getText().trim();
                List<String> lines = new ArrayList<>();
                int found = 0;
                // Datei lesen und in den Speicher laden
                try (BufferedReader reader = new BufferedReader(new FileReader("Usernames.txt"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(user) || line.startsWith(username)){
                            line += " | " + portnumber;
                            System.out.println(line + user + username);
                            found++;

                        }
                        lines.add(line);
                    }

                    if (found == 2){
                        System.out.println("test2");
                        userContacts.clear();
                        userContacts.put(user,portnumber++);
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Usernames.txt"))) {
                            for (String l : lines) {
                                writer.write(l);
                                writer.newLine();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        userBtnMaker(username, userContacts);

                    }

                }catch (Exception e){
                    System.out.println(e);
                }


            }
        }
    }
}

