import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuController {

    @FXML
    private Button taskManagementBtn;

    @FXML
    private Button pomodoroBtn;

    @FXML
    private Button trackStreakBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button workTogetherBtn;

    @FXML
    public void loadTaskManagement(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/sidebar.fxml", 900, 600);
        m.getStage().setResizable(true);
    }

    @FXML
    public void loadPomodoro(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/pomodoro.fxml", 800, 600);
    }

    @FXML
    public void loadTrackStreak(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/streak2.fxml", 900 , 700);
    }

    @FXML
    public void userLogout(ActionEvent event) throws IOException {
        Session.clear();
        App m = new App();
        m.changeScene("fxmlFiles/sample.fxml",600,400);
    }

    @FXML
    void workTogether(ActionEvent event) {
        try {
            // Create a new stage for the project manager
            Stage projectStage = new Stage();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmlFiles/project.fxml"));

            VBox root = loader.load();
            
            ProjectController controller = loader.getController();
            controller.setPrimaryStage(projectStage);
            
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("styleCSS/project.css").toExternalForm());

            
            projectStage.setTitle("Project Manager");
            projectStage.setScene(scene);
            projectStage.setMinWidth(800);
            projectStage.setMinHeight(600);
            
            // Handle application close
            projectStage.setOnCloseRequest(e -> {
                controller.disconnect();
                projectStage.close();
            });
            
            projectStage.show();
            
            // Show connection dialog
            controller.showConnectionDialog();
        } catch (Exception e) {
            e.printStackTrace();
            // You might want to show an error alert here
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Project Manager");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    
}