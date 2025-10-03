import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application{
    private static Stage stage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        primaryStage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("fxmlFiles/sample.fxml"));
        primaryStage.setTitle("OrganizeMe");
        primaryStage.setScene(new Scene(root,600,400));
        primaryStage.show();
    }

    public Stage getStage(){
        return stage;
    }

    public void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        stage.getScene().setRoot(pane);
        getStage().setResizable(false);
    }
    
    public void changeScene(String fxml, int width, int height) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        Scene scene = new Scene(pane, width, height); 
        stage.setScene(scene); 
        getStage().setResizable(false);
    }

    public void changeScene(String fxml, NavigateController controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent pane = loader.load();
        controller = loader.getController();
        stage.getScene().setRoot(pane);
        getStage().setResizable(false);
    }
        

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        launch(args);
    }
}
