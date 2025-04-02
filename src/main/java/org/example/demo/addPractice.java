package org.example.demo;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;


public class addPractice {
    public static void  enterPracticeQ() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Practice Question Details");
        dialog.setHeaderText("Please enter your Question:");

        // Create the GridPane layout
        GridPane grid = new GridPane();
        grid.setPrefSize(500, 400);
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Create labels and text fields
        TextField IdField = new TextField();
        TextField LevelField = new TextField();
        TextField TitleField = new TextField();
        TextArea DescArea = new TextArea();
        TextField AnswerField = new TextField();

        // Add components to the grid
        Label Insert = new Label("Your Question");
        Insert.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        Insert.setStyle("-fx-background-color: #4aa146; -fx-highlight-text-fill: white ");
        grid.add(Insert, 0, 0);

        Label idLabel = new Label("ID:");
        idLabel.setFont(Font.font("Tahoma", 15));
        grid.add(idLabel, 0, 1);
        grid.add(IdField, 1, 1);

        Label TitleLabel = new Label("Title:");
        TitleLabel.setFont(Font.font("Tahoma", 15));
        grid.add(TitleLabel, 0, 2);
        grid.add(TitleField, 1, 2);

        Label lvlLabel = new Label("Level (1-5):");
        Label infoLvl = new Label("invalid levels will be labeled at 1");
        lvlLabel.setFont(Font.font("Tahoma", 15));
        infoLvl.setFont(Font.font("Tahoma", 12));

        grid.add(lvlLabel, 0, 3);
        grid.add(LevelField, 1, 3);
        grid.add(infoLvl, 1, 4);

        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Tahoma", 15));
        grid.add(descLabel, 0, 5);
        grid.add(DescArea, 1, 5);

        Label ansLabel = new Label("Answer:");
        ansLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label infoLabel = new Label("(if unsolved, leave blank)");
        infoLabel.setFont(Font.font("Verdana", 12));

        AnswerField.setPrefWidth(100);
        TitleField.setPrefWidth(100);
        IdField.setPrefWidth(30);

        grid.add(ansLabel, 0, 6);
        grid.add(infoLabel, 1, 7);
        grid.add(AnswerField, 1, 6);

        DescArea.setPrefWidth(300);
        DescArea.setPrefHeight(200);
        DescArea.setWrapText(true);
        // Add the grid to the dialog
        dialog.getDialogPane().setContent(grid);

        // Add OK and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ArrayList<String> input;
        // Show the dialog and process input
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String id = IdField.getText();
            String title = TitleField.getText();
            String desc = DescArea.getText();
            String ans = AnswerField.getText();
            String lvl = LevelField.getText();
            System.out.println(id + " " + title + " " + desc + " " + ans + " " + lvl);
            //String id, int level, String title, String desc, String answer
            ClientApplication.sendQuestion(id, lvlValid(lvl), title, desc, ans);

        } else {
            System.out.println("User canceled input.");
        }
    }
    private static int lvlValid(String id){
        try{
            int idInt = Integer.parseInt(id);
            if(idInt > 5 || idInt < 1){
                return 1;
            }
            return idInt;
        } catch(NumberFormatException e){
            return 1;
        }
    }
}
