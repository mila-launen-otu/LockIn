package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ClientApplication extends Application {
    private static String username = "";
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static TextArea textArea;
    private static TextField textField;
    private static TextField loginTextField;
    private static Stage stage;
    private static double width = 800;
    private static double height = 600;

    public static double getWidth(){
        return width;
    }
    public static double getHeight(){
        return height;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        //start with asking for username

        username = JOptionPane.showInputDialog("Enter Username");

        socket = new Socket("localhost", 5001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        out.println(username);

        GridPane gridPane = new GridPane();

        // whiteboard pane
        GridPane whiteboardPane = new GridPane();
        whiteboardPane.setStyle("-fx-border-color: lightblue; -fx-border-width: 2");
        whiteboardPane.setPrefSize(width/2+20, height);
        GridPane.setConstraints(whiteboardPane, 0, 0, 1, 2); // Spans 2 rows

        // chat pane
        GridPane chatPane = new GridPane();
        chatPane.setStyle("-fx-border-color: lightgreen; -fx-border-width: 2;");
        chatPane.setPrefSize(width/2-20, height/2);
        GridPane.setConstraints(chatPane, 1, 0);

        Label chatLabel = new Label("Chat");
        chatLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setStyle("-fx-highlight-text-fill: lightblue; -fx-border-width: 2");

        ScrollPane scrollPane = new ScrollPane();
        textArea.setPrefSize(width/2-20, height/2-15);

        textField = new TextField();
        textField.setEditable(true);
        textField.setPrefSize(width/2-20-70, 15);

        Button sendBtn = new Button("Send");
        sendBtn.setPrefSize(70, 15);
        HBox hbox = new HBox(10);
        hbox.getChildren().add(textField);
        hbox.getChildren().add(sendBtn);// No issue

        chatPane.add(chatLabel, 0, 0);
        chatPane.add(textArea,0,1);
        chatPane.add(hbox, 0, 2);

        // practice question Pane
        GridPane practicePane = new GridPane();
        practicePane.setStyle("-fx-border-color: lightcoral; -fx-border-width: 2;");
        practicePane.setPrefSize(width/2-20, height/2);
        GridPane.setConstraints(practicePane, 1, 1);
        VBox questList = new VBox();

        questList.setSpacing(5);

        ArrayList<practicePanels.PracticeQuestion> insertPanels = practicePanels.InitalizeJsonLoad();
        for (practicePanels.PracticeQuestion pq : insertPanels) {
            questList.getChildren().add(pq.UI);
        }

        ScrollPane PracticeScroll = new ScrollPane();
        PracticeScroll.setContent(questList);
        PracticeScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);  // Always show vertical scrollbar
        PracticeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);  // Never show horizontal scrollbar

        // Set the ScrollPane to fill the available vertical space
        PracticeScroll.setFitToWidth(true);  // Ensures the VBox fits to the width of the ScrollPane
        PracticeScroll.setFitToHeight(true);

        Label PracticeLabel = new Label("Practice Problems");
        PracticeLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        practicePane.add(PracticeLabel,0,1);
        practicePane.add(PracticeScroll,0,2);


        // Add panes to the gridPane
        gridPane.add(whiteboardPane, 0, 0, 1, 2);  // Left pane spans 2 rows
        gridPane.add(chatPane, 1, 0);   // Top-right pane
        gridPane.add(practicePane, 1, 1); // Bottom-right pane

        // Set column and row constraints for resizing
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Scene scene = new Scene(gridPane, width, height);
        primaryStage.setTitle(username +"'s Workspace");
        primaryStage.setScene(scene);
        primaryStage.show();

        //window closing listener:
        primaryStage.setOnCloseRequest(event -> {
            try{
                if(socket != null && !socket.isClosed()){
                    socket.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        });

        textField.setOnAction(event -> sendMessage());
        sendBtn.setOnAction(event -> sendMessage());

        new Thread(()->{
            try{
                String message;
                while((message = in.readLine()) != null){
                    if(message.contains("ANS-")){
                        String msg = message.substring(4);
                        textArea.appendText(username + " solved "+msg+"\n");

                    }
                    else{
                        textArea.appendText(message + "\n");
                    }
                }
            }catch(IOException e){
                if(!socket.isClosed()){
                    e.printStackTrace();
                }
            } finally {
                try{
                    if(socket != null && !socket.isClosed()){
                        socket.close();
                    }
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private static void sendMessage(){
        String message = textField.getText();
        if(!message.isEmpty()){
            out.println(message);
            textField.clear();
        }
    }
    public static void sendAnswerMessage(String message, String id){
        if(!message.isEmpty()){
            out.println("ANS-" + username + " answered " + id + "!" );
        }
    }

    public static void main(String[] args) {
        launch();
    }
}