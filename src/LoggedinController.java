import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LoggedinController{

    @FXML
    private Button logout;
    
    @FXML
    private Button btn1;
    @FXML
    private Button btn2;
    @FXML
    private Button btn3;

    @FXML
    void launchBtn1(ActionEvent event) throws IOException {
        App m =new App();
        String user = Session.getUsername();
        System.out.println("Current user: " + user);
        m.changeScene("fxmlFiles/todo1.fxml");
    }
    @FXML
    void launchBtn2(ActionEvent event) throws IOException {
        App m =new App();
        String user = Session.getUsername();
        System.out.println("Current user: " + user);
        m.changeScene("fxmlFiles/todo2.fxml");
    }
    @FXML
    void launchBtn3(ActionEvent event) throws IOException {
        App m =new App();
        String user = Session.getUsername();
        System.out.println("Current user: " + user);
        m.changeScene("fxmlFiles/todo3.fxml");
    }

    @FXML
    public void userLogout(ActionEvent event) throws IOException{
        Session.clear();
        App m = new App();
        m.changeScene("fxmlFiles/sample.fxml",600,400);
    }

}
