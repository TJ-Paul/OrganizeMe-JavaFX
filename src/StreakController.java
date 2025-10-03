import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StreakController {

    @FXML private Label currentStreakLabel;
    @FXML private Label bestStreakLabel;
    @FXML private Label totalDaysLabel;
    @FXML private Label monthlyStreakLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar streakProgressBar;
    @FXML private GridPane calendarGrid;
    @FXML private VBox achievementsContainer;
    @FXML private Button markTodayBtn;
    @FXML private Button resetStreakBtn;
    @FXML private Button openTasksBtn;
    @FXML private Button openPomodoroBtn;
    @FXML private Button backToMenuBtn;

    private Set<LocalDate> completedDays = new HashSet<>();
    private int currentStreak = 0;
    private int bestStreak = 0;
    private LocalDate lastActiveDate = null;
    private boolean todayCompleted = false;

    // Milestone thresholds
    private final int[] MILESTONES = {7, 14, 30, 50, 100, 365};
    private final String[] MILESTONE_NAMES = 
    {"Week Warrior", "Fortnight Fighter", "Monthly Master", 
    "Fifty Fighter", "Century Champion", "Year Yearner"};

    @FXML
    public void initialize() {
        loadStreakData();
        updateDisplay();
        createCalendarView();
        checkStreakBreak();
        updateAchievements();
    }

    @FXML
    public void markTodayComplete(ActionEvent event) {
        LocalDate today = LocalDate.now();
        
        if (!todayCompleted) {
            completedDays.add(today);
            todayCompleted = true;
            
            // Update streak
            if (lastActiveDate == null || lastActiveDate.equals(today.minusDays(1))) {
                currentStreak++;
            } else if (!lastActiveDate.equals(today)) {
                // Streak was broken, reset to 1
                currentStreak = 1;
                showStreakBreakAlert();
            }
            
            lastActiveDate = today;
            
            // Update best streak
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
                showNewRecordAlert();
            }
            
            saveStreakData();
            updateDisplay();
            createCalendarView();
            updateAchievements();
            
            markTodayBtn.setText("✓ Completed Today");
            markTodayBtn.setDisable(true);
            markTodayBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 10;");
            
            showAlert("Great job!", "Today marked as complete! Keep the streak going! 🔥", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void resetStreak(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reset Streak");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("This will reset your current streak to 0. Your best streak record will remain unchanged.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentStreak = 0;
            lastActiveDate = null;
            saveStreakData();
            updateDisplay();
            updateAchievements();
            
            showAlert("Streak Reset", "Your streak has been reset. Start fresh tomorrow! 💪", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void openTaskManagement(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/sidebar.fxml", 900, 600);
        m.getStage().setResizable(true);
    }

    @FXML
    public void openPomodoro(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/pomodoro.fxml", 800, 600);
    }

    @FXML
    public void backToMenu(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/menu.fxml", 600, 400);
    }

    private void updateDisplay() {
        currentStreakLabel.setText(String.valueOf(currentStreak));
        bestStreakLabel.setText(String.valueOf(bestStreak));
        totalDaysLabel.setText(String.valueOf(completedDays.size()));
        
        // Calculate monthly streak
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        long monthlyCount = completedDays.stream()
            .filter(date -> !date.isBefore(startOfMonth))
            .count();
        monthlyStreakLabel.setText(String.valueOf(monthlyCount));
        
        // Update progress bar
        updateProgressBar();
        
        // Update today button
        LocalDate today = LocalDate.now();
        todayCompleted = completedDays.contains(today);
        if (todayCompleted) {
            markTodayBtn.setText("✓ Completed Today");
            markTodayBtn.setDisable(true);
            markTodayBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 10;");
        } else {
            markTodayBtn.setText("✓ Mark Today Complete");
            markTodayBtn.setDisable(false);
            markTodayBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 10;");
        }
    }

    private void updateProgressBar() {
        int nextMilestone = getNextMilestone();
        if (nextMilestone > 0) {
            double progress = (double) currentStreak / nextMilestone;
            streakProgressBar.setProgress(Math.min(progress, 1.0));
            progressLabel.setText("Progress to " + nextMilestone + " days: " + currentStreak + "/" + nextMilestone);
        } else {
            streakProgressBar.setProgress(1.0);
            progressLabel.setText("All milestones achieved! Keep going! 🏆");
        }
    }

    private int getNextMilestone() {
        for (int milestone : MILESTONES) {
            if (currentStreak < milestone) {
                return milestone;
            }
        }
        return -1; // All milestones achieved
    }

    private void createCalendarView() {
        calendarGrid.getChildren().clear();
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(27); // Show last 28 days (4 weeks)
        
        // Add day labels
        String[] dayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayLabels[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-font-size: 12;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(25);
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Add date squares
        for (int week = 0; week < 4; week++) {
            for (int day = 0; day < 7; day++) {
                LocalDate date = startDate.plusDays(week * 7 + day);
                Label dateLabel = new Label();
                dateLabel.setPrefSize(25, 25);
                dateLabel.setAlignment(Pos.CENTER);
                dateLabel.setText(String.valueOf(date.getDayOfMonth()));
                
                if (completedDays.contains(date)) {
                    dateLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-weight: bold; -fx-font-size: 10;");
                } else if (date.equals(today)) {
                    dateLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-weight: bold; -fx-font-size: 10;");
                } else {
                    dateLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d; -fx-background-radius: 3; -fx-font-size: 10;");
                }
                
                calendarGrid.add(dateLabel, day, week + 1);
            }
        }
    }

    private void updateAchievements() {
        achievementsContainer.getChildren().clear();
        
        for (int i = 0; i < MILESTONES.length; i++) {
            int milestone = MILESTONES[i];
            String name = MILESTONE_NAMES[i];
            
            HBox achievementBox = new HBox(10);
            achievementBox.setAlignment(Pos.CENTER_LEFT);
            achievementBox.setPadding(new Insets(5));
            
            Label icon = new Label();
            Label nameLabel = new Label(name);
            Label descLabel = new Label("(" + milestone + " days)");
            
            if (bestStreak >= milestone) {
                icon.setText("🏆");
                achievementBox.setStyle("-fx-background-color: #f1c40f; -fx-background-radius: 5;");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                descLabel.setStyle("-fx-text-fill: #2c3e50;");
            } else {
                icon.setText("🔒");
                achievementBox.setStyle("-fx-background-color: #bdc3c7; -fx-background-radius: 5;");
                nameLabel.setStyle("-fx-text-fill: #7f8c8d;");
                descLabel.setStyle("-fx-text-fill: #7f8c8d;");
            }
            
            achievementBox.getChildren().addAll(icon, nameLabel, descLabel);
            achievementsContainer.getChildren().add(achievementBox);
        }
    }

    private void checkStreakBreak() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        if (lastActiveDate != null && !lastActiveDate.equals(yesterday) && !lastActiveDate.equals(today) && currentStreak > 0) {
            // Streak was broken
            long daysSinceLastActive = ChronoUnit.DAYS.between(lastActiveDate, today);
            if (daysSinceLastActive > 1) {
                currentStreak = 0;
                saveStreakData();
                showStreakBreakAlert();
            }
        }
    }

    private void showStreakBreakAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Streak Broken!");
        alert.setHeaderText("Your streak has been reset");
        alert.setContentText("Don't worry! Every expert was once a beginner. Start building your streak again today! 💪");
        alert.showAndWait();
    }

    private void showNewRecordAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Personal Record!");
        alert.setHeaderText("Congratulations! 🎉");
        alert.setContentText("You've set a new personal best streak of " + bestStreak + " days! Keep up the amazing work!");
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void saveStreakData() {
        if (Session.getUsername() == null) return;
        
        File file = new File("streak_" + Session.getUsername() + ".txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("CURRENT_STREAK:" + currentStreak);
            writer.println("BEST_STREAK:" + bestStreak);
            if (lastActiveDate != null) {
                writer.println("LAST_ACTIVE:" + lastActiveDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            writer.println("COMPLETED_DAYS:");
            for (LocalDate date : completedDays) {
                writer.println(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        } catch (IOException e) {
            System.err.println("Error saving streak data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStreakData() {
        if (Session.getUsername() == null) return;
        
        File file = new File("streak_" + Session.getUsername() + ".txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean readingCompletedDays = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("CURRENT_STREAK:")) {
                    currentStreak = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("BEST_STREAK:")) {
                    bestStreak = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("LAST_ACTIVE:")) {
                    lastActiveDate = LocalDate.parse(line.split(":", 2)[1]);
                } else if (line.equals("COMPLETED_DAYS:")) {
                    readingCompletedDays = true;
                } else if (readingCompletedDays && !line.trim().isEmpty()) {
                    completedDays.add(LocalDate.parse(line.trim()));
                }
            }
        } catch (IOException | NumberFormatException | java.time.format.DateTimeParseException e) {
            System.err.println("Error loading streak data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Public method to be called from other controllers when tasks/pomodoro are completed
    public static void recordActivity() {
        // This would be called from task completion or pomodoro completion
        // For now, we'll just mark today as active if it hasn't been marked
        String username = Session.getUsername();
        if (username == null) return;
        
        File file = new File("streak_" + username + ".txt");
        Set<LocalDate> completedDays = new HashSet<>();
        LocalDate today = LocalDate.now();
        
        // Load existing data
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean readingCompletedDays = false;
                
                while ((line = reader.readLine()) != null) {
                    if (line.equals("COMPLETED_DAYS:")) {
                        readingCompletedDays = true;
                    } else if (readingCompletedDays && !line.trim().isEmpty()) {
                        completedDays.add(LocalDate.parse(line.trim()));
                    }
                }
            } catch (IOException | java.time.format.DateTimeParseException e) {
                System.err.println("Error loading streak data for activity recording: " + e.getMessage());
            }
        }
        
        // Add today if not already added
        if (!completedDays.contains(today)) {
            completedDays.add(today);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                if (!file.exists() || file.length() == 0) {
                    writer.println("COMPLETED_DAYS:");
                }
                writer.println(today.format(DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (IOException e) {
                System.err.println("Error recording activity: " + e.getMessage());
            }
        }
    }
}