package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.*;
import java.net.*;

public class WhiteboardPanel {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    private Canvas canvas;
    private GraphicsContext gc;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    private Color currentColor = Color.BLACK;
    private double currentLineWidth = 3;

    private void setStrokeProperties(Color color, int width){
        gc.setStroke(color);
        gc.setLineWidth(width);
        this.currentColor = color;
        this.currentLineWidth = width;
    }

    public BorderPane getWhiteboardPane() {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();

        gc.setLineWidth(3);

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Set color switcher
        Button blackButton = new Button();
        blackButton.setStyle("-fx-background-color: black; -fx-border-color: black;");
        blackButton.setPrefSize(50, 50);

        Button redButton = new Button();
        redButton.setStyle("-fx-background-color: red; -fx-border-color: red;");
        redButton.setPrefSize(50, 50);

        Button greenButton = new Button();
        greenButton.setStyle("-fx-background-color: green; -fx-border-color: green;");
        greenButton.setPrefSize(50, 50);

        Button blueButton = new Button();
        blueButton.setStyle("-fx-background-color: blue; -fx-border-color: blue;");
        blueButton.setPrefSize(50, 50);

        Button eraserButton = new Button();
        eraserButton.setStyle("-fx-background-color: white; -fx-border-color: white;");
        eraserButton.setPrefSize(50, 50);

//        Button saveButton = new Button("Save");
//        saveButton.setPrefSize(50, 50);

        Button clearButton = new Button("Clear");
        clearButton.setPrefSize(50, 50);

        //Layout for buttons
        HBox buttonBox = new HBox(10, blackButton, redButton, greenButton, blueButton, eraserButton, clearButton); buttonBox.setStyle("-fx-padding: 10px; -fx-background-color: lightgray;");

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setTop(buttonBox);

        blackButton.setOnAction(e -> setStrokeProperties(Color.BLACK, 3));
        redButton.setOnAction(e -> setStrokeProperties(Color.RED, 3));
        greenButton.setOnAction(e -> setStrokeProperties(Color.GREEN, 3));
        blueButton.setOnAction(e -> setStrokeProperties(Color.BLUE, 3));
        eraserButton.setOnAction(e -> setStrokeProperties(Color.WHITE, 25));
        clearButton.setOnAction(e -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });


        // Setup action listeners for mouse events
        setupEventHandlers();

        // Connect to server
        connectToServer();

        return root;
    }

    private void setupEventHandlers() {
        // Handle mouse press (record as "click" in CSV)
        canvas.setOnMousePressed(e -> {
            double x = e.getX();
            double y = e.getY();

            // Draw a point
            gc.beginPath();
            gc.lineTo(x, y);
            gc.stroke();

            // Send to server if connected
            if (isConnected) {
                sendCoordinates(x, y, "click");
            }
        });

        // Handle mouse drag
        canvas.setOnMouseDragged(e -> {
            double x = e.getX();
            double y = e.getY();

            // Draw a line
            gc.lineTo(x, y);
            gc.stroke();

            // Send to server if connected
            if (isConnected) {
                sendCoordinates(x, y, "drag");
            }
        });
    }


    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                isConnected = true;
                System.out.println("Connected to server");

                // Listen for updates from server
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String finalInput = inputLine;
                    // Update UI on JavaFX thread
                    Platform.runLater(() -> processCoordinates(finalInput));
                }
            } catch (IOException e) {
                System.err.println("Error connecting to server: " + e.getMessage());
                isConnected = false;
            }
        }).start();
    }

    private void sendCoordinates(double x, double y, String type) {
        // Format: x,y,type,color,lineWidth
        String colorString = currentColor.toString();
        String data = x + "," + y + "," + type + "," + colorString + "," + currentLineWidth;
        out.println(data);
    }

    private void processCoordinates(String data) {
        String[] parts = data.split(",");
        if (parts.length >= 3) {
            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                String type = parts[2];

                // Handle color and line width if they exist
                if (parts.length >= 5) {
                    try {
                        Color color = Color.web(parts[3]);
                        double lineWidth = Double.parseDouble(parts[4]);
                        gc.setStroke(color);
                        gc.setLineWidth(lineWidth);
                    } catch (Exception e) {
                        // If there's an error parsing color or width, use default
                        System.err.println("Error parsing color or width: " + e.getMessage());
                    }
                }

                if ("click".equals(type)) {
                    // Start a new path
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.lineTo(x, y);
                    gc.stroke();
                } else if ("drag".equals(type)) {
                    // Continue current path
                    gc.lineTo(x, y);
                    gc.stroke();
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid coordinate data: " + data);
            }
        }
    }

    private void closeConnection() {
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}