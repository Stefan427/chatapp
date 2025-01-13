package com.example.demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatServer implements Runnable{
    int port;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public ChatServer(int port){
        this.port = port;
    }
    @Override
    public  void run() {
        System.out.println("Chat Server started...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String username = in.readLine();
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;

                while ((message = in.readLine()) != null) {
                   messageSpeichern(message,username);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            if (writer != out) { // Do not send back to the sender
                                writer.println(username + ": " + message); // Broadcast with sender's name
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
        public void messageSpeichern(String message, String username){
            List<String> lines = new ArrayList<>();
            boolean found = false;

            // Datei lesen und in den Speicher laden
            try (BufferedReader reader = new BufferedReader(new FileReader("Chatlog.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(socket.getLocalPort() + "|")) {
                        // Zeile aktualisieren
                        line += username + ": " + message + "|";
                        found = true;
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Neue Zeile hinzufügen, falls nicht gefunden
            if (!found) {
                lines.add(socket.getLocalPort() + "|" + username + ": " + message + "|");
            }

            // Datei mit aktualisiertem Inhalt überschreiben
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("Chatlog.txt"))) {
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
