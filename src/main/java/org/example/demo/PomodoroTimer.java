package org.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PomodoroTimer extends VBox {
    private int timeRemaining;
    private Timeline pomoTimer;

    // UI Components
    private final Label timerLabel = new Label();
    private final Label focusLabel = new Label("Focus 0/4");
    private final Label breakLabel = new Label("Break 0/4");

    // Buttons
    private final Button startButton = new Button("Start");
    private final Button pauseButton = new Button("Pause");

    // State Variables
    private boolean isRunning = false;
    private boolean onBreak = false;

    // Pomodoro Counters
    private int focusCount = 0;
    private int breakCount = 0;

    // Pomodoro Timer Settings
    private static final int FOCUS_TIME = 25 * 60; // 25 minutes
    private static final int SHORT_BREAK_TIME = 5 * 60; // 5 minutes
    private static final int LONG_BREAK_TIME = 25 * 60; // 25 minutes

    // Constructor
    public PomodoroTimer() {
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setStyle("-fx-padding: 40;");

        // Set styles for labels
        timerLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        focusLabel.setStyle("-fx-text-fill: #3366cc; -fx-font-size: 16px;");
        breakLabel.setStyle("-fx-text-fill: #00a676; -fx-font-size: 16px;");

        // Initialize timer
        timeRemaining = FOCUS_TIME;
        timerLabel.setText(formatTime(timeRemaining));

        // Event listeners for buttons
        startButton.setOnAction(e -> startTimer(true));
        pauseButton.setOnAction(e -> pauseTimer(true));
        Button skipButton = new Button("Skip");
        skipButton.setOnAction(e -> skipSession(true));
        Button endButton = new Button("End");
        endButton.setOnAction(e -> endSession(true));

        // Arrange buttons in a row horizontally
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(skipButton, startButton, endButton);

        // Final layout
        getChildren().addAll(timerLabel, focusLabel, breakLabel, buttonBox);
    }

    // Start the timer
    private void startTimer(boolean broadcast) {
        // Stop any existing timer
        if (pomoTimer != null) {
            pomoTimer.stop();
        }

        // Create a new timer
        pomoTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            timerLabel.setText(formatTime(timeRemaining));
            if (timeRemaining <= 0) {
                pomoTimer.stop();
                isRunning = false;
                completeSession();
            }
        }));

        // Set the timer to run indefinitely
        pomoTimer.setCycleCount(Timeline.INDEFINITE);
        pomoTimer.play();
        isRunning = true;
        switchToPauseButton();

        // Broadcast the start command to all connected users
        if (broadcast) {
            ClientApplication.sendPomodoroCommand("POMO-START");
        }
    }

    // Pause the timer
    private void pauseTimer(boolean broadcast) {
        // Stop the timer if it's running
        if (pomoTimer != null && isRunning) {
            pomoTimer.pause();
            isRunning = false;
            switchToStartButton();

            // Broadcast the pause command to all connected users
            if (broadcast) {
                ClientApplication.sendPomodoroCommand("POMO-PAUSE");
            }
        }
    }

    // Skip the current session
    private void skipSession(boolean broadcast) {
        // Stop the timer if it's running
        if (pomoTimer != null) {
            pomoTimer.stop();
            pomoTimer = null;
        }

        // Reset the timer state
        isRunning = false;

        String modeToSend;
        int timeToSend;

        // Update the focus and break counts
        if (!onBreak) {
            focusCount++; // Increment focus count
            focusLabel.setText("Focus " + focusCount + "/4"); // Update text holder
            onBreak = true; // Set to break mode
            timeRemaining = (focusCount % 4 == 0) ? LONG_BREAK_TIME : SHORT_BREAK_TIME; // Set time
            modeToSend = "BREAK"; // Set mode to break to send
            timeToSend = timeRemaining; // Set time to send
        } else { // If already on break
            breakCount++; // Increment break count
            breakLabel.setText("Break " + breakCount + "/4"); // Update text holder
            onBreak = false; // Set to focus mode
            timeRemaining = FOCUS_TIME; // Set time to focus time
            modeToSend = "FOCUS"; // Set mode to focus to send
            timeToSend = timeRemaining; // Set time to send
        }

        // Update the timer label
        timerLabel.setText(formatTime(timeRemaining));
        switchToStartButton();

        // Broadcast the skip command to all connected users
        if (broadcast) {
            // Format the message to send
            String message = String.format(
                    "POMO-SETSTATE:%s:%d:%d:%d",
                    modeToSend,
                    timeToSend,
                    focusCount,
                    breakCount
            );
            System.out.println("Broadcasting: " + message); // Debugging
            ClientApplication.sendPomodoroCommand(message); // Send the message
        }
    }

    // Complete the current session
    private void completeSession() {
        // If not on break, increase focus count, set label, and start break
        if (!onBreak) {
            focusCount++;
            focusLabel.setText("Focus " + focusCount + "/4");
            startBreak();
        } else { // If already on break, increase break count, set label, and start focus
            breakCount++;
            breakLabel.setText("Break " + breakCount + "/4");
            startFocus();
        }
    }

    // End the session
    private void endSession(boolean broadcast) {
        // Stop the timer if it's running
        if (pomoTimer != null) {
            pomoTimer.stop();
        }

        // Reset the timer back to start
        focusCount = 0;
        breakCount = 0;
        onBreak = false;
        isRunning = false;

        // Broadcast the end command to all connected users
        if (broadcast) {
            ClientApplication.sendPomodoroCommand("POMO-END");
        }

        // Reset the timer to initial state
        timeRemaining = FOCUS_TIME;
        timerLabel.setText(formatTime(timeRemaining));
        focusLabel.setText("Focus 0/4");
        breakLabel.setText("Break 0/4");

        switchToStartButton(); // Switch from end to start button.
    }

    // Start focus session
    private void startFocus() {
        // Reset the timer to set focus time
        onBreak = false;
        timeRemaining = FOCUS_TIME;
        timerLabel.setText(formatTime(timeRemaining));
        switchToStartButton();
    }

    // Start break session
    private void startBreak() {
        onBreak = true; // Set to break mode

        if (focusCount % 4 == 0) { // If 4 focus sessions completed, set long break
            timeRemaining = LONG_BREAK_TIME;
        } else { // Else, set short break
            timeRemaining = SHORT_BREAK_TIME;
        }

        // Update the timer label
        timerLabel.setText(formatTime(timeRemaining));
        switchToStartButton(); // Switch from break to start button.
    }

    // Switch to pause button
    private void switchToPauseButton() {
        HBox buttonBox = (HBox) getChildren().get(3);
        buttonBox.getChildren().set(1, pauseButton);
    }

    // Switch to start button
    private void switchToStartButton() {
        HBox buttonBox = (HBox) getChildren().get(3);
        buttonBox.getChildren().set(1, startButton);
    }

    // Format time in MM:SS format
    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    // External methods to control the timer
    public void externalStart(boolean broadcast) {
        Platform.runLater(() -> {
            if (!isRunning) startTimer(broadcast);
        });
    }

    // External methods to control the timer
    public void externalPause(boolean broadcast) {
        Platform.runLater(() -> pauseTimer(broadcast));
    }

//    // External methods to control the timer
//    public void externalSkip(boolean broadcast) {
//        Platform.runLater(() -> skipSession(broadcast));
//    }

    // External methods to control the timer
    public void externalEnd(boolean broadcast) {
        Platform.runLater(() -> endSession(broadcast));
    }

    // External method to set the state of the timer
    public void setState(String mode, int seconds, int focus, int brk) {
        // Stop the timer if it's running
        if (pomoTimer != null) {
            pomoTimer.stop();
        }

        // Reset the timer state
        this.timeRemaining = seconds;
        this.focusCount = focus;
        this.breakCount = brk;
        this.onBreak = mode.equals("BREAK");

        // Update the timer label and focus/break labels
        timerLabel.setText(formatTime(timeRemaining));
        focusLabel.setText("Focus " + focusCount + "/4");
        breakLabel.setText("Break " + breakCount + "/4");

        isRunning = false;
        switchToStartButton(); // Switch from end to start button.
    }
}
