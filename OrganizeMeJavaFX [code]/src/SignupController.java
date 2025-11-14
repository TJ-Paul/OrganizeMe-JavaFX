import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    
    @FXML
    private Button login;
    
    @FXML
    private Button signup;
    
    @FXML
    private TextField username;
    
    @FXML
    private TextField email;
    
    @FXML
    private PasswordField password;
    
    @FXML
    private Label wrongPass;

    @FXML
    private Label wronglogin;

    @FXML
    void userLogin(ActionEvent event) throws IOException {
        App m = new App();
        m.changeScene("fxmlFiles/sample.fxml",600,400);
    }
    
    @FXML
    void userSignup(ActionEvent event) throws IOException {
        App m = new App();

        String user = username.getText().trim();
        String pass = password.getText().trim();
        String mail = email.getText().trim();
        
        
        if (user.isEmpty() || pass.isEmpty() || mail.isEmpty()) {
            if (user.isEmpty()) {
                wronglogin.setText("Username is required.");
            } else if (mail.isEmpty()) {
                wronglogin.setText("Email is required.");
            } else if (pass.isEmpty()) {
                wronglogin.setText("Password is required.");
            }
        } else if (!mail.endsWith("@gmail.com")) {
            wronglogin.setText("not a proper mail address");
        } else {
            try (FileWriter fw = new FileWriter("users.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
                
                out.println(user + "," + mail + "," + pass);
            } catch (IOException e) {
                wronglogin.setText("Failed to save user data.");
                e.printStackTrace();
                return;
            }
            
            Session.setUsername(user); 
            wronglogin.setText(""); 
            m.changeScene("fxmlFiles/menu.fxml",600,400);
            m.getStage().setResizable(true);
        }
    }



}
