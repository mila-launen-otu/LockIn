package org.example.demo;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class question{
    String id;
    int level;
    String title;
    String desc;
    String answer;

    public question(String id, int level, String title, String desc, String answer) {
        this.id = id;
        this.level = level;
        this.title = title;
        this.desc = desc;
        this.answer = answer;
    }
}

public class practicePanels {
    public static ArrayList<PracticeQuestion> listOfQuestions;
    public static String mainFilePath = "src/main/java/questionData.json";

    public static class PracticeQuestion {
        private double w;
        private double h;

        private String id;
        private String title;
        private String desc;
        private String answer;
        private int level;
        private boolean solved;
        private Button tryButton;

        public GridPane UI;

        PracticeQuestion(String id, String title, String desc, String answer, int level) {
            this.w = (ClientApplication.getWidth()) / 2 - 30;
            this.h = 50;

            if(answer.equals("") || answer == null) {
                this.answer = "";
                this.solved = false;
            }
            else{
                this.answer = answer;
                this.solved = true;
            }
            this.id = id;
            this.title = title;
            this.desc = desc;
            this.level = level;

            GridPane gridQuestion = new GridPane();
            gridQuestion.setPrefSize(w, h);

            HBox hBox = new HBox();
            hBox.setSpacing(5);
            VBox vBox = new VBox();
            vBox.setSpacing(5);

            Label idLabel = new Label("ID: " +id + " Level: " +level);
            idLabel.setStyle("-fx-font-weight: bold");

            Label titleLabel = new Label(this.title);
            Label descLabel = new Label(this.desc);

            vBox.getChildren().addAll(idLabel, titleLabel, descLabel);
            vBox.setPrefSize(w - 100 , h-10);

            tryButton = new Button();
            tryButton.setStyle("-fx-font-weight: bold");
            tryButton.setPrefSize(100, h-10);

            changeButton();
            hBox.getChildren().add(vBox);
            hBox.getChildren().add(tryButton);
            gridQuestion.getChildren().add(hBox);

            if(this.level == 1){
                gridQuestion.setStyle("-fx-background-color: #a6ffb2; -fx-border-color: #323232");
            } else if (this.level == 2){
                gridQuestion.setStyle("-fx-background-color: #d7ffa6; -fx-border-color: #323232");
            } else if (this.level == 3){
                gridQuestion.setStyle("-fx-background-color: #fff0a6; -fx-border-color: #323232");
            } else if(this.level == 4){
                gridQuestion.setStyle("-fx-background-color: #ffc8a6; -fx-border-color: #323232");
            } else if(this.level == 5){
                gridQuestion.setStyle("-fx-background-color: #ffb3a6; -fx-border-color: #323232");
            }
            this.UI = gridQuestion;

            tryButton.setOnAction(event -> {
                if(this.solved == false){
                    String ans = JOptionPane.showInputDialog(this.desc);
                    this.answer = ans;
                    modifyJsonAns(this.id, ans);
                    ClientApplication.sendAnswerMessage(ans, this.title);
                    this.solved  = true;
                    changeButton();
                }
                else{
                    String ans = JOptionPane.showInputDialog(this.desc);
                    String message;
                    if(!ans.equals(this.answer)){
                       message = "Incorrect. Correct Answer: " + this.answer;
                    }
                    else{
                        message = "Correct!";
                    }
                    JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.INFORMATION_MESSAGE);

                }
            });
        }
        public String getId(){return id;}
        public int getLevel(){return level;}
        public String getTitle(){return title;}
        public String getDesc(){return desc;}
        public String getAnswer(){return answer;}

        private void changeButton(){
            if (!this.solved) {
                tryButton.setText("Solve me!");
                tryButton.setStyle("-fx-font-weight: bold; -fx-background-color: #ff674b; -fx-text-fill: white;");

            } else {
                tryButton.setText("Solved");
                tryButton.setStyle("-fx-font-weight: bold; -fx-background-color: #a8e87f; -fx-text-fill: white;");
            }
        }
    }


    public static ArrayList<PracticeQuestion> InitalizeJsonLoad() {
        ArrayList<PracticeQuestion> quest_Loader_Inital = new ArrayList<>();
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(mainFilePath)) {
            // Parse JSON into a JsonObject
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Get the "questions" array
            JsonArray questionsArray = jsonObject.getAsJsonArray("questions");

            // Loop through each question
            for (int i = 0; i < questionsArray.size(); i++) {
                JsonObject question = questionsArray.get(i).getAsJsonObject();

                // Access and print the values
                String id = question.get("id").getAsString();
                Integer level = question.get("level").getAsInt();
                String title = question.get("title").getAsString();
                String desc = question.get("desc").getAsString();
                String answer = question.get("answer").getAsString();

                // Check if the "level" exists (it might be missing in some questions)
                // Print question details
                PracticeQuestion practiceQuestion = new PracticeQuestion(id, title, desc, answer, level);
                quest_Loader_Inital.add(practiceQuestion);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        listOfQuestions = quest_Loader_Inital;
        return quest_Loader_Inital;
    }
    public static void addQuestionToArray(String id, int level, String title, String desc, String answer){
        PracticeQuestion newQuestion = new PracticeQuestion(id, title, desc, answer, level);
        listOfQuestions.add(newQuestion);
    }
    public static void addQuestionToJson(PracticeQuestion pq) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonData;

        try {
            String json = Files.exists(Paths.get(mainFilePath)) ? Files.readString(Paths.get(mainFilePath)) : "{}";
            jsonData = JsonParser.parseString(json).getAsJsonObject();
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            return;
        }

        JsonArray questions = jsonData.has("questions") ? jsonData.getAsJsonArray("questions") : new JsonArray();
        JsonObject newQuestion = new JsonObject();
        newQuestion.addProperty("id", pq.getId());
        newQuestion.addProperty("level", pq.getLevel());
        newQuestion.addProperty("title", pq.getTitle());
        newQuestion.addProperty("desc", pq.getTitle());
        newQuestion.addProperty("answer", pq.getAnswer());

        questions.add(newQuestion);
        jsonData.add("questions", questions);

        try {
            Files.write(Paths.get(mainFilePath), gson.toJson(jsonData).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void modifyJsonAns(String questionId, String ans) {
        String filePath = mainFilePath; // Path to your JSON file
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Create Gson instance

        try {
            // Step 1: Read the JSON file into a JsonObject
            FileReader reader = new FileReader(filePath);
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close(); // Close the reader

            // Step 2: Get the "questions" array from the JsonObject
            JsonArray questionsArray = jsonObject.getAsJsonArray("questions");

            // Step 3: Loop through each question and find the one with the matching ID
            for (int i = 0; i < questionsArray.size(); i++) {
                JsonObject question = questionsArray.get(i).getAsJsonObject();
                System.out.println("Compare: "+question.get("id") + " vs " + questionId);
                if (question.get("id").getAsString().equals(questionId)) {
                    // Step 4: Modify the answer of the matched question
                    question.addProperty("answer", ans); // Update the answer

                    // Step 5: Write the updated JSON back to the file
                    FileWriter writer = new FileWriter(filePath);
                    gson.toJson(jsonObject, writer); // Convert the entire updated JSON and save
                    writer.close(); // Close the writer

                    System.out.println("Answer updated for question ID " + questionId);
                    return; // Exit after updating the answer
                }
            }
            System.out.println("Question with ID " + questionId + " not found.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}