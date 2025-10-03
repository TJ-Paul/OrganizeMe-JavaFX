import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HomeController implements NavigateController{

    @FXML
    private Button login;

    @FXML
    private PasswordField password;

    @FXML
    private Button signup;

    @FXML
    private TextField username;

    @FXML
    private Label wrongPass;

    @FXML
    private Label wronglogin;

    public void userLogin(ActionEvent e) throws IOException{
        checkLogin();
    }

    @FXML
    public void checkLogin() throws IOException {
        App m = new App();
        String inputUser = username.getText().trim();
        String inputPass = password.getText().trim();

        if (inputUser.isEmpty() || inputPass.isEmpty()) {
            wronglogin.setText("Please enter your data.");
            return;
        }

        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String storedUser = parts[0];
                    String storedPass = parts[2];

                    if (inputUser.equals(storedUser) && inputPass.equals(storedPass)) {
                        found = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            wronglogin.setText("Error reading user data.");
            e.printStackTrace();
            return;
        }

        if (found) {
            wronglogin.setText("Success!");
            Session.setUsername(inputUser); 
            m.changeScene("fxmlFiles/menu.fxml", 600, 400); 
        } else {
            wronglogin.setText("Wrong username or password!");
        }
    }

    @Override
    public void navigateTo(String fxmlPath, int width, int height) throws IOException {
        App m = new App();
        m.changeScene(fxmlPath, width, height);
    }
    
    @Override
    public void navigateTo(String fxmlPath) throws IOException {
        App m = new App();
        m.changeScene(fxmlPath);
    }
    
    @FXML
    public void userSignup(ActionEvent event) throws IOException {
        navigateTo("fxmlFiles/signup.fxml");
    }
}