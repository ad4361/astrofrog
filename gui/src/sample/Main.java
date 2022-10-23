package sample;

import sample.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;

public class Main extends Application {

    private Model model;


    // used to test db connection
    private Scene tempScene() throws SQLException {

        Pane pane = new Pane();
        Scene scene = new Scene(pane, 500, 400);

        String query = "select * from \"User\" limit 1";
        ResultSet rs = sample.PostgresSSH.executeSelect(query);
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

        Parent root = FXMLLoader.load(getClass().getResource("Welcome.fxml"));
        stage.setTitle("RibBit :: AstroFrog");

        Scene scene = new Scene(root);
        stage.setScene(scene);

        //stage.setScene(tempScene());

        stage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
