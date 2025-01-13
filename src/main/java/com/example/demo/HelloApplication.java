package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if (!isServerRunning()) {
            new Thread(ChatServer::startServer).start();
        } else {
            System.out.println("Server läuft bereits.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 620, 440);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
    private static boolean isServerRunning() {
        try (Socket socket = new Socket("127.0.0.1", 12345)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public static void main(String[] args) {
        launch();
    }
}