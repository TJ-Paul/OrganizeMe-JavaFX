import java.io.IOException;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

public class PomodoroController {

    @FXML
    private Label timerLabel;

    @FXML
    private Label sessionLabel;

    @FXML
    private Label sessionsLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button startBtn;

    @FXML
    private Button pauseBtn;

    @FXML
    private Button resetBtn;

    @FXML
    private Button workBtn;

    @FXML
    private Button shortBreakBtn;

    @FXML
    private Button longBreakBtn;

    @FXML
    private Button backToMenuBtn;

    private Timeline timeline;
    private int totalSeconds;
    private int currentSeconds;
    private int completedSessions = 0;
    private boolean isRunning = false;
    private PomodoroMode currentMode = PomodoroMode.WORK;

    // Timer durations 
    private static final int WORK_DURATION = 25 * 60; // 25 minutes
    private static final int SHORT_BREAK_DURATION = 5 * 60; // 5 minutes
    private static final int LONG_BREAK_DURATION = 15 * 60; // 15 minutes

    private enum PomodoroMode {
        WORK, SHORT_BREAK, LONG_BREAK
    }

    @FXML
    public void initialize() {
        setWorkMode();
        updateButtons();
    }

    @FXML
    public void startTimer(ActionEvent event) {
        if (!isRunning) {
            isRunning = true;
            startCountdown();
            updateButtons();
        }
    }

    @FXML
    public void pauseTimer(ActionEvent event) {
        if (isRunning) {
            isRunning = false;
            if (timeline != null) {
                timeline.stop();
            }
            updateButtons();
        }
    }

    @FXML
    public void resetTimer(ActionEvent event) {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
        }
        
        switch (currentMode) {
            case WORK:
                currentSeconds = WORK_DURATION;
                break;
            case SHORT_BREAK:
                currentSeconds = SHORT_BREAK_DURATION;
                break;
            case LONG_BREAK:
                currentSeconds = LONG_BREAK_DURATION;
                break;
        }
        
        updateDisplay();
        updateButtons();
    }

    @FXML
    public void setWorkMode(ActionEvent event) {
        setWorkMode();
    }

    @FXML
    public void setShortBreakMode(ActionEvent event) {
        setShortBreakMode();
    }

    @FXML
    public void setLongBreakMode(ActionEvent event) {
        setLongBreakMode();
    }

    @FXML
    private Button viewStreakBtn;

    @FXML
    private Button viewTasksBtn;

    @FXML
    public void viewStreak(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Back to Streak!!");
        alert.setHeaderText("Wanna break the focus?");
        alert.setContentText("This will reset the timer!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {        
            if (timeline != null) {
                timeline.stop();
            }
            App m = new App();
            m.changeScene("fxmlFiles/streak2.fxml", 900, 700);
        }
    }

    @FXML
    public void viewTasks(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Back to Task Management!!");
        alert.setHeaderText("Wanna break the focus?");
        alert.setContentText("This will reset the timer!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (timeline != null) {
                timeline.stop();
            }
            App m = new App();
            m.changeScene("fxmlFiles/sidebar.fxml", 900, 600);
            m.getStage().setResizable(true);
        }
    }

    @FXML
    public void backToMenu(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Back to Menu!!");
        alert.setHeaderText("Wanna break the focus?");
        alert.setContentText("This will reset the timer!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
                if (timeline != null) {
                    timeline.stop();
                }                
                App m = new App();
                m.changeScene("fxmlFiles/menu.fxml", 600, 400);
        }
    }

    private void setWorkMode() {
        currentMode = PomodoroMode.WORK;
        totalSeconds = WORK_DURATION;
        currentSeconds = WORK_DURATION;
        sessionLabel.setText("Work Session");
        updateDisplay();
        resetTimerState();
    }

    private void setShortBreakMode() {
        currentMode = PomodoroMode.SHORT_BREAK;
        totalSeconds = SHORT_BREAK_DURATION;
        currentSeconds = SHORT_BREAK_DURATION;
        sessionLabel.setText("Short Break");
        updateDisplay();
        resetTimerState();
    }

    private void setLongBreakMode() {
        currentMode = PomodoroMode.LONG_BREAK;
        totalSeconds = LONG_BREAK_DURATION;
        currentSeconds = LONG_BREAK_DURATION;
        sessionLabel.setText("Long Break");
        updateDisplay();
        resetTimerState();
    }

    private void resetTimerState() {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
        }
        updateButtons();
    }

    private void startCountdown() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            currentSeconds--;
            updateDisplay();
            
            if (currentSeconds <= 0) {
                timerFinished();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void timerFinished() {
        isRunning = false;
        timeline.stop();
        
        if (currentMode == PomodoroMode.WORK) {
            completedSessions++;
            sessionsLabel.setText("Sessions completed: " + completedSessions);
            
            // Record activity for streak tracking
            StreakController.recordActivity();
            
            // Auto-switch to appropriate break
            if (completedSessions % 4 == 0) {
                setLongBreakMode();
                showAlert("Work session complete!", "Time for a long break! (15 minutes)\n🔥 Activity recorded for your streak!");
            } else {
                setShortBreakMode();
                showAlert("Work session complete!", "Time for a short break! (5 minutes)\n🔥 Activity recorded for your streak!");
            }
        } else {
            setWorkMode();
            showAlert("Break finished!", "Ready for another work session? (25 minutes)");
        }
        
        updateButtons();
    }

    private void updateDisplay() {
        int minutes = currentSeconds / 60;
        int seconds = currentSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        
        double progress = 1.0 - (double) currentSeconds / totalSeconds;
        progressIndicator.setProgress(progress);
    }

    private void updateButtons() {
        startBtn.setDisable(isRunning);
        pauseBtn.setDisable(!isRunning);
        
        // Disable mode buttons when timer is running
        workBtn.setDisable(isRunning);
        shortBreakBtn.setDisable(isRunning);
        longBreakBtn.setDisable(isRunning);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}