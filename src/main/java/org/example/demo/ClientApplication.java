package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;


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
        TabPane tabPane = new TabPane();

        GridPane gridPane = new GridPane();

        // Create tabs
        Tab workspaceTab = new Tab("Workspace", gridPane);
        Tab whiteboardTab = new Tab("Whiteboard");

        tabPane.getTabs().addAll(workspaceTab, whiteboardTab);

        WhiteboardPanel whiteboardPanel = new WhiteboardPanel();
        whiteboardTab.setContent(whiteboardPanel.getWhiteboardPane());

        // chat pane
        GridPane chatPane = new GridPane();
        chatPane.setStyle("-fx-border-color: lightgreen; -fx-border-width: 2;");
        chatPane.setPrefSize(width/2-20, height/2);
        GridPane.setConstraints(chatPane, 1, 0);

        Label chatLabel = new Label("Chat");
        chatLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        textArea.setStyle("fx-font-weight: bold;-fx-text-fill: #4aa146;");

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
        Button newQuestion = new Button("Add New Question");
        newQuestion.setPrefSize(width/2-20, 35);

        newQuestion.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        newQuestion.setStyle("fx-font-weight: bold; -fx-background-color: #1c1c1c; -fx-text-fill: #4aa146;");

        newQuestion.setOnAction(event ->{
            //TO DO: add Question to Json file
            addPractice.enterPracticeQ();

        });

        practicePane.add(PracticeLabel,0,1);
        practicePane.add(PracticeScroll,0,2);
        practicePane.add(newQuestion,0,3);

        // Add panes to the gridPane
        gridPane.add(chatPane, 1, 0);
        gridPane.add(practicePane, 1, 1);

        workspaceTab.setContent(gridPane);

        VBox root = new VBox(tabPane);
        Scene scene = new Scene(root, width, height);

        primaryStage.setTitle(username + "'s Workspace");
        primaryStage.setScene(scene);
        primaryStage.show();

        //window closing listener:
        primaryStage.setOnCloseRequest(event -> {
            try{
                File csvFile = new File("whiteboard_data.csv");
                if(csvFile.exists()){
                    if(csvFile.delete()){
                        System.out.println("CSV file deleted.");
                    } else{
                        System.out.println("Cannot delete CSV file.");
                    }
                }

                if(socket != null && !socket.isClosed()){
                    socket.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        });

        textField.setOnAction(event -> sendMessage());
        sendBtn.setOnAction(event -> sendMessage());

        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    final String finalMessage = message;
                    Platform.runLater(() -> {
                        if (finalMessage.contains("ANS-")) {
                            String msg = finalMessage.substring(4);
                            textArea.appendText(username + " solved " + msg + "\n");
                        } else {
                            textArea.appendText(finalMessage + "\n");
                        }
                    });
                }
            } catch (IOException e) {
                // existing error handling code
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