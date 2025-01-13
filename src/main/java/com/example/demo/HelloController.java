package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class HelloController {
    private Thread serverThread;
    int port = 12345; // default

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

            // Pass the PrintWriter and Socket to ChatRoom controller
            ChatRoom chatRoomController = fxmlLoader.getController();
            chatRoomController.setClientConnection(out, socket, username);

            Stage stage = (Stage) connectBtn.getScene().getWindow();
            stage.setScene(firstScene);
            stage.show();
        } catch (Exception e1) {
            System.out.println(e1);; // Handle connection errors
        }
    }
    private boolean isPortInUse(int port) {
        try (ServerSocket tempSocket = new ServerSocket(port)) {
            return false; // Port ist frei
        } catch (Exception e) {
            return true; // Port ist belegt
        }
    }
}