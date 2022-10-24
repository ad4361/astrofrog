package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button loginButton;
    @FXML
    private Button goBackButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginMessageLabel;

    public void switchToWelcomeScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Welcome.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToMainPageScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public User createSelf(String username) {
        String query = "SELECT * FROM \"User\" WHERE username = '" + username + "'";
        try {
            ResultSet rs = PostgresSSH.executeSelect(query);
            String uname = null, firstname = null, lastname = null, email = null;
            Date dob = null, creationDate = null;
            LocalDateTime lastAccessDate = null;
            while (rs.next()) {
                uname = rs.getString("username");
                firstname = rs.getString("firstname");
                lastname = rs.getString("lastname");
                dob = rs.getDate("dob");
                email = rs.getString("email");
                creationDate = rs.getDate("creationDate");
                lastAccessDate = LocalDateTime.now();
            }

            // need to update lastAccessDate in db

            return new User(uname, firstname, lastname, email, dob, creationDate, lastAccessDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void login(ActionEvent event) throws IOException, SQLException {

        if (usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
            loginMessageLabel.setText("Please enter username and password.");
        } else {

            try {
                // implement db security stuff
                String query = "SELECT count(1) FROM \"User\" WHERE username = '" +
                        usernameField.getText() + "' AND password = '" +
                        passwordField.getText() + "'";
                String anotherquery = "SELECT USERNAME, SALT, HASHEDPASS FROM \"User\" WHERE username = '" +
                        usernameField.getText() + "'";

//                ResultSet rs = PostgresSSH.executeSelect(query);
                ResultSet test = PostgresSSH.executeSelect(anotherquery);
                if (test != null) {
                    while (test.next()) {
                        if (test.getString("salt") != null) {
                            String toHash = passwordField.getText() + test.getString("salt");
                            System.out.println(toHash);
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            md.update(toHash.getBytes());
                            BigInteger hash = new BigInteger(1, md.digest());
                            String check = hash.toString(16);
                            if (check.equals(test.getString(3))) {
                                System.out.println(test.getString(3));
                                User self = createSelf(usernameField.getText());
                                Model.setSelf(self);
                                switchToMainPageScene(event);
                            }
                            else {
                                loginMessageLabel.setText("Invalid credentials. Try again.");
                            }
                        }
                    }
                }
 //               if (rs != null) {
 //                   while (rs.next()) {
//                        if (rs.getInt(1) == 1) {
//                            loginMessageLabel.setText("Welcome!");
//                            User self = createSelf(usernameField.getText());
//                            Model.setSelf(self);
 //                           switchToMainPageScene(event);
 //                       } else {
 //                           loginMessageLabel.setText("Invalid credentials. Try again.");
 //                       }
 //                   }
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
