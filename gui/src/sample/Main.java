package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Main extends Application {

    private Model model;


    /**
     * Creates and returns the scene for the login/signup page.
     *
     * @param login - true if login, false if signup
     * @return
     */
    private Scene createLoginSignupScene(boolean login) {

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 500, 400);

        // login/signup message: top
        Label topLabel;
        if (login) {
            topLabel = new Label("Login");
        } else {
            topLabel = new Label("Signup");
        }
        borderPane.setTop(topLabel);
        BorderPane.setAlignment(topLabel, Pos.CENTER);
        topLabel.setFont(Font.font("Cambria", 45));

        // text fields for login/signup: center
        VBox vbox = new VBox();
        TextField usernameField = new TextField();
        usernameField.setPromptText("username");
        TextField passwordField = new TextField();
        passwordField.setPromptText("password");
        vbox.getChildren().addAll(usernameField, passwordField);
        // additional signup boxes
        if (!login) {
            TextField firstnameField = new TextField();
            firstnameField.setPromptText("first name");

            TextField lastnameField = new TextField();
            lastnameField.setPromptText("last name");

            TextField dobField = new TextField();
            dobField.setPromptText("date of birth: MM/DD/YY");

            TextField emailField = new TextField();
            emailField.setPromptText("email");
            vbox.getChildren().addAll(firstnameField, lastnameField, dobField, emailField);
        }
        borderPane.setCenter(vbox);

        // login/signup button: bottom
        Button button;
        if (login) {
            button = new Button("Login");
            button.setOnAction((event) -> this.model.login());
        } else {
            button = new Button("Signup");
            button.setOnAction((event)-> this.model.signup());
        }
        borderPane.setBottom(button);
        BorderPane.setAlignment(button, Pos.CENTER);

        return scene;
    }

    /**
     * Function that creates and returns the welcome scene that is first seen upon starting
     * up RibBit.
     *
     * @return the welcome scene prompting login or signup
     */
    private Scene createWelcomeScene(Stage stage) {

        BorderPane borderPane = new BorderPane();
        Scene welcomeScene = new Scene(borderPane, 500, 400);

        // welcome text
        Label topLabel = new Label("Welcome to RibBit");
        borderPane.setTop(topLabel);
        BorderPane.setAlignment(topLabel, Pos.CENTER);
        topLabel.setFont(Font.font("Cambria", 45));

        // create login and signup buttons, add them to b-pane center
        HBox hButtonBox = new HBox();
        Button login = new Button("Login");
        Button signup = new Button("Sign Up");
        hButtonBox.getChildren().addAll(login, signup);
        hButtonBox.setAlignment(Pos.CENTER);
        borderPane.setCenter(hButtonBox);
        BorderPane.setAlignment(hButtonBox, Pos.CENTER);

        // event handlers for login and signup buttons
        login.setOnAction((event) -> stage.setScene(createLoginSignupScene(true)));
        signup.setOnAction((event) -> stage.setScene(createLoginSignupScene(false)));

        // TODO: make it look nice

        return welcomeScene;

    }

    // used to test db connection
    private Scene tempScene() throws SQLException {

        Pane pane = new Pane();
        Scene scene = new Scene(pane, 500, 400);

        String query = "select * from \"User\" limit 20";
        ResultSet rs = PostgresSSH.executeSelect(query);
        ResultSetMetaData rsMetaData = rs.getMetaData();

        int count = rsMetaData.getColumnCount();
        System.out.println(count);
        for (int i = 1; i <= count; i++) {
            System.out.println(rsMetaData.getColumnName(i));
        }
        VBox vbox = new VBox();

        while (rs.next()) {
            String username = rs.getString("username");
            String password = rs.getString("password");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            Date dob = rs.getDate("dob");
            String email = rs.getString("email");
            Label label = new Label(username + " " + password + " " + firstname + " "
                    + lastname + " " + dob + " " + email);
            vbox.getChildren().addAll(label);
        }
        pane.getChildren().addAll(vbox);

        return scene;
    }

    @Override
    public void start(Stage stage) throws Exception{

        stage.setTitle("RibBit :: AstroFrog");
        //stage.setScene(createWelcomeScene(stage));
        stage.setScene(tempScene());
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
