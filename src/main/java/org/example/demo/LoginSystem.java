package org.example.demo;

import com.google.gson.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static javafx.application.Platform.exit;

public class LoginSystem {
    public class User {
        String username;
        String password;

        User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        String getUsername() {
            return username;
        }
    }

    private Dialog<ButtonType> dialog;
    private GridPane mainGrid;
    private Button existingUser;
    private Button newUser;

    public LoginSystem() {
        dialog = new Dialog<>();
        dialog.setTitle("LockIn Login System");
        dialog.setHeaderText("Welcome to LockIn!");

        // Create main layout
        mainGrid = new GridPane();
        mainGrid.setPrefSize(400, 200);
        mainGrid.setPadding(new Insets(10));
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);


        // Create buttons
        existingUser = new Button("Login");
        newUser = new Button("Create Account");

        existingUser.setStyle("-fx-font-weight: bold; -fx-background-color: #00985c; -fx-text-fill: white;");
        existingUser.setPrefSize(180,60);

        newUser.setStyle("-fx-font-weight: bold; -fx-background-color: #00985c; -fx-text-fill: white;");
        newUser.setPrefSize(180,60);
        // Add buttons to main grid
        mainGrid.add(existingUser, 0, 0);
        mainGrid.add(newUser, 1, 0);

        // Button actions
        existingUser.setOnAction(e -> showLoginForm());
        newUser.setOnAction(e -> showNewAccount());

        // Display the dialog
        dialog.getDialogPane().setContent(mainGrid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            exit();
        }
    }

    private void showLoginForm() {
        GridPane loginGrid = new GridPane();
        loginGrid.setPrefSize(400,400);
        loginGrid.setPadding(new Insets(10));
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);

        Label insertUser = new Label("Welcome Back!");
        insertUser.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        loginGrid.add(insertUser, 0, 0);

        Label details = new Label("Enter your details:");
        details.setFont(Font.font("Tahoma", 12));
        loginGrid.add(details, 0, 1);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Tahoma", 15));
        TextField usernameField = new TextField();
        loginGrid.add(usernameLabel, 0, 2);
        loginGrid.add(usernameField, 0, 3);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Tahoma", 15));
        PasswordField passwordField = new PasswordField();
        loginGrid.add(passwordLabel, 0, 4);
        loginGrid.add(passwordField, 0, 5);

        // Back button to return to main screen
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> dialog.getDialogPane().setContent(mainGrid));

        // Submit button
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            if (usernameField.getText().isEmpty()) {
                System.out.println("Username cannot be empty");
            } else {
                User u1 = searchUser(usernameField.getText(), passwordField.getText());
                if (u1.getUsername().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid username or password", "Attention!", JOptionPane.INFORMATION_MESSAGE);
                    System.out.println("Invalid username or password");
                } else {
                    ClientApplication.setUsername(usernameField.getText());
                    dialog.setResult(ButtonType.OK);  // Close dialog on successful input
                }
            }
        });

        loginGrid.add(backButton, 0, 6);
        loginGrid.add(submitButton, 1, 5);

        // Update the dialog content dynamically
        dialog.getDialogPane().setContent(loginGrid);
    }

    private void showNewAccount() {
        GridPane registerGrid = new GridPane();
        registerGrid.setPadding(new Insets(10));
        registerGrid.setHgap(10);
        registerGrid.setVgap(10);

        Label insertUser = new Label("Create a new Account:");
        insertUser.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        registerGrid.add(insertUser, 0, 0);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Tahoma", 15));
        TextField usernameField = new TextField();
        registerGrid.add(usernameLabel, 0, 1);
        registerGrid.add(usernameField, 0, 2);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Tahoma", 15));
        PasswordField passwordField = new PasswordField();
        registerGrid.add(passwordLabel, 0, 3);
        registerGrid.add(passwordField, 0, 4);

        // Back button to return to main screen
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> dialog.getDialogPane().setContent(mainGrid));

        // Submit button
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                System.out.println("Username and Password cannot be empty");
            } else {
                addAccount(usernameField.getText(), passwordField.getText());
                ClientApplication.setUsername(usernameField.getText());
                dialog.setResult(ButtonType.OK);  // Close dialog on successful input
            }
        });

        registerGrid.add(backButton, 0, 5);
        registerGrid.add(submitButton, 1, 5);

        // Update the dialog content dynamically
        dialog.getDialogPane().setContent(registerGrid);
    }

    private static void addAccount(String username, String password) {
        String filePath = "src/main/java/users.json";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonData;

        try {
            String json = Files.exists(Paths.get(filePath)) ? Files.readString(Paths.get(filePath)) : "{}";
            jsonData = JsonParser.parseString(json).getAsJsonObject();
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            return;
        }

        JsonArray users = jsonData.has("users") ? jsonData.getAsJsonArray("users") : new JsonArray();
        JsonObject newUser = new JsonObject();
        newUser.addProperty("username", username);
        newUser.addProperty("password", password);

        users.add(newUser);
        jsonData.add("users", users);  // Fixed key from "questions" to "users"

        try {
            Files.write(Paths.get(filePath), gson.toJson(jsonData).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User searchUser(String username, String password) {
        String filePath = "src/main/java/users.json";
        Gson gson = new Gson();

        try {
            FileReader reader = new FileReader(filePath);
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close();

            JsonArray usersArray = jsonObject.getAsJsonArray("users");  // Fixed key from "questions" to "users"

            for (JsonElement userElement : usersArray) {
                JsonObject user = userElement.getAsJsonObject();
                if (user.get("username").getAsString().equals(username) &&
                        user.get("password").getAsString().equals(password)) {
                    return new User(username, password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new User("", "");
    }
}
