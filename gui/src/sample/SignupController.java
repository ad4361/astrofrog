package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;

public class SignupController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker dobField;
    @FXML
    private TextField emailField;
    @FXML
    private Label warningLabel;


    public void switchToWelcomeScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Welcome.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
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

    public void signup(ActionEvent event) {
        if (usernameField.getText().isBlank() || passwordField.getText().isBlank() || firstNameField.getText().isBlank()
        || lastNameField.getText().isBlank() || dobField.getValue() == null || emailField.getText().isBlank()) {
            warningLabel.setText("Please enter all information.");
        }
        else {
            try {
            String queryUsernameAvailable = "SELECT count(1) FROM \"User\" WHERE username=?";
            PreparedStatement stmt = PostgresSSH.connection.prepareStatement(queryUsernameAvailable);
            stmt.setString(1, usernameField.getText());
            ResultSet rs = stmt.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    if (rs.getInt(1) == 1) {
                        warningLabel.setText("Username already taken, please try another.");
                    }
                    else {
                        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
                        int length = (int) (Math.random() * 9 + 2);
                        StringBuilder saltMaker = new StringBuilder(length);
                        for (int i = 0; i < length; i++){
                            int index = (int)(AlphaNumericString.length()  * Math.random());
                            saltMaker.append(AlphaNumericString.charAt(index));
                        }
                        String salt = saltMaker.toString();
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
                        String hashedPass = hash.toString(16);

                        String insertQuery = "INSERT INTO \"User\" (username, firstname, lastname, dob, email, creationdate, lastaccessdate," +
                                " salt, hashedpass) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        PreparedStatement statement = PostgresSSH.connection.prepareStatement(insertQuery);
                        statement.setString(1, usernameField.getText());
                        statement.setString(2, firstNameField.getText());
                        statement.setString(3, lastNameField.getText());
                        statement.setDate(4, Date.valueOf(dobField.getValue()));
                        statement.setString(5, emailField.getText());
                        statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                        statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                        statement.setString(8, salt);
                        statement.setString(9, hashedPass);
                        statement.executeUpdate();

                        User self = createSelf(usernameField.getText());
                        Model.setSelf(self);
                        switchToMainPageScene(event);
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

}
