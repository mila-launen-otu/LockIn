package org.example.demo;
//libraries to import
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import static javafx.application.Platform.exit;

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

        LoginSystem newLogin =  new LoginSystem();
        if(username.isEmpty()){
            exit();
        }
        System.out.println("CLIENT: Successful username attempt");
        socket = new Socket("localhost", 5001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        out.println(username);
        TabPane tabPane = new TabPane();

        GridPane gridPane = new GridPane();

        // Create tabs
        Tab workspaceTab = new Tab("Workspace", gridPane);
        Tab whiteboardTab = new Tab("Whiteboard");

        // Create pomodoro timer
        PomodoroTimer pomodoroTimer = new PomodoroTimer();

        workspaceTab.setClosable(false);
        whiteboardTab.setClosable(false);

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

        textArea.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane();
        textArea.setPrefSize(width/2-20, height/2-15);

        textField = new TextField();
        textField.setEditable(true);
        textField.setPrefSize(width/2-20-70, 15);

        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("custom-button");
        sendBtn.setPrefSize(80, 40);
        HBox hbox = new HBox(10);
        hbox.getChildren().add(textField);
        hbox.getChildren().add(sendBtn);// No issue

        chatPane.add(chatLabel, 0, 0);
        chatPane.add(textArea,0,1);
        chatPane.add(hbox, 0, 2);

        // practice question Pane
        GridPane practicePane = new GridPane();
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
        Button newQuestion = new Button("Add New Question");
        newQuestion.setPrefSize(width/2-20, 35);

        newQuestion.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        newQuestion.setOnAction(event ->{
            //TO DO: add Question to Json file
            addPractice.enterPracticeQ();

        });

        practicePane.add(PracticeLabel,0,1);
        practicePane.add(PracticeScroll,0,2);
        practicePane.add(newQuestion,0,3);

        // Add chat, practice and pomodoro timer to the grid pane
        gridPane.add(chatPane, 0, 1);
        gridPane.add(practicePane, 0, 2);
        gridPane.add(pomodoroTimer, 1, 1, 1, 2);

        // Set the grid pane to fill the available space
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col1, col2);

        workspaceTab.setContent(gridPane);

        VBox root = new VBox(tabPane);
        Scene scene = new Scene(root, width, height);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/workSpaceStyle.css")).toExternalForm());
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
                        }
                        else if(finalMessage.contains("NEWQ")) {
                            String[] add_new_question = finalMessage.split("_");
                            //String id, String title, String desc, String answer, int level
                            practicePanels.PracticeQuestion new_question = new practicePanels.PracticeQuestion(add_new_question[1], add_new_question[2],add_new_question[3],add_new_question[4],Integer.parseInt(add_new_question[5]));
                            practicePanels.addQuestionToJson(new_question);
                            practicePanels.listOfQuestions.add(new_question);

                            practicePanels.PracticeQuestion pq = practicePanels.listOfQuestions.getLast();
                            questList.getChildren().add(pq.UI);
                            // Handle incoming messages related to Pomodoro timer synchronization
                        } else if (finalMessage.startsWith("POMO-SETSTATE")) {
                            // Expected format: POMO-SETSTATE:<MODE>:<SECONDS>:<FOCUS_COUNT>:<BREAK_COUNT>
                            String[] parts = finalMessage.split(":"); // Split by colon
                            if (parts.length == 5) { // If we have the right number of parts
                                // Extract the session mode: "FOCUS" or "BREAK"
                                String mode = parts[1];

                                // Extract the timer value in seconds
                                int seconds = Integer.parseInt(parts[2]);

                                // Extract the current focus session count
                                int focusCount = Integer.parseInt(parts[3]);

                                // Extract the current break session count
                                int breakCount = Integer.parseInt(parts[4]);

                                // Update the local PomodoroTimer instance with the new session state
                                // This ensures all users sync to the same session, time, and counts
                                Platform.runLater(() -> pomodoroTimer.setState(mode, seconds, focusCount, breakCount));
                            }
                            // Remote command to start the Pomodoro timer
                        } else if (finalMessage.contains("POMO-START")) {
                            pomodoroTimer.externalStart(false);
                            // Remote command to pause the Pomodoro timer
                        } else if (finalMessage.contains("POMO-PAUSE")) {
                            pomodoroTimer.externalPause(false);
                            // Remote command to end/reset the Pomodoro timer
                        } else if (finalMessage.contains("POMO-END")) {
                            pomodoroTimer.externalEnd(false);
                            // Remote command to skip the current Pomodoro session
                        } else if (!finalMessage.contains("POMO-")) {
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
    }//hello!test git
    public static void setUsername(String username){
        ClientApplication.username = username;
    }
    //String id, int level, String title, String desc, String answer
    public static void sendQuestion(String id, int level, String title, String desc, String answer){
        out.println("NEWQ_"+id+"_"+title+"_"+desc+"_"+answer+"_"+level);
        out.println("New Question: " + title + ", By: " + username);
    }

    // Send commands to the Pomodoro timer
    public static void sendPomodoroCommand(String command) {
        if (out != null) { // Ensure out is initialized
            out.println(command); // Send the command to the server
        }
    }

    public static void main(String[] args) {
        launch();
    }
}