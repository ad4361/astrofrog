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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class SignupController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button signupButton;
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

    public void signup(ActionEvent event) throws IOException, SQLException {
        if (usernameField.getText().isBlank() || passwordField.getText().isBlank() || firstNameField.getText().isBlank()
        || lastNameField.getText().isBlank() || dobField.getValue() == null || emailField.getText().isBlank()) {
            warningLabel.setText("Please enter all information.");
        }
        else {
            try {
            String queryUsernameAvailable = "SELECT count(1) FROM \"User\" WHERE username = '" +
                    usernameField.getText()+"'";
            ResultSet rs = PostgresSSH.executeSelect(queryUsernameAvailable);
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
                        Statement st = PostgresSSH.connection.createStatement();
                        String query = "INSERT INTO \"User\" (username, firstname, lastname, dob, email, creationdate, lastaccessdate," +
                                " salt, hashedpass) VALUES ('" +usernameField.getText()+"' , '" +firstNameField.getText()+"' , '"
                                +lastNameField.getText()+"', '"+dobField.getValue()+"', '"+emailField.getText()+"' ,'"+
                                LocalDateTime.now()+"', '"+LocalDateTime.now()+"', '"+salt+"', '"+hashedPass+"')";
                        st.executeUpdate(query);
                        User self = createSelf(usernameField.getText());
                        Model.setSelf(self);
                        switchToMainPageScene(event);
                    }
                }
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            Statement st = PostgresSSH.connection.createStatement();
            String updateAccess = "UPDATE \"User\" SET LASTACCESSDATE = '" +lastAccessDate+ "' WHERE username = '" + username + "'";
            int newTime = st.executeUpdate(updateAccess);
            if (newTime < 1) {
                System.out.println("Did not work, check the statement or if the user exists!");
            }
            return new User(uname, firstname, lastname, email, dob, creationDate, lastAccessDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
