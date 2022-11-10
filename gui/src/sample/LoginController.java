package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;

public class LoginController {

    private Stage stage;
    private Scene scene;
    private Parent root;

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
        String query = "SELECT * FROM \"User\" WHERE username=?";
        try {
            PreparedStatement stmt = PostgresSSH.connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
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
            String updateAccess = "UPDATE \"User\" SET LASTACCESSDATE = ? WHERE username = ?";
            PreparedStatement statement = PostgresSSH.connection.prepareStatement(updateAccess);
            statement.setTimestamp(1, Timestamp.valueOf(lastAccessDate));
            statement.setString(2, username);
            int newTime = statement.executeUpdate();
            if (newTime < 1) {
                System.out.println("Did not work, check the statement or if the user exists!");
            }
            return new User(uname, firstname, lastname, email, dob, creationDate, lastAccessDate);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

    public void login(ActionEvent event) {

        if (usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
            loginMessageLabel.setText("Please enter username and password.");
        } else {

            try {
                String anotherquery = "SELECT USERNAME, SALT, HASHEDPASS FROM \"User\" WHERE username=?";
                PreparedStatement stmt = PostgresSSH.connection.prepareStatement(anotherquery);
                stmt.setString(1, usernameField.getText());
                ResultSet test = stmt.executeQuery();

                if (test != null) {
                    while (test.next()) {
                        if (test.getString("salt") != null) {
                            String salt = test.getString("salt");
                            StringBuilder combo = new StringBuilder();
                            int j = 0;
                            for (int i = 0; i < passwordField.getText().length(); i++) {
                                combo.append(passwordField.getText().charAt(i));
                                if (j < salt.length()) {
                                    combo.append(salt.charAt(j));
                                    j++;
                                }
                            }
                            String toHash = combo.toString();
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            md.update(toHash.getBytes());
                            BigInteger hash = new BigInteger(1, md.digest());
                            String check = hash.toString(16);
                            if (check.equals(test.getString(3))) {
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

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    PostgresSSH.connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}
