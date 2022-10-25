package sample;

import com.sun.webkit.Timer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainPageController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label hiLabel;

    @FXML
    public void initialize() {
        hiLabel.setText("Hi, " + Model.self.getUsername() + "!");
    }

    @FXML
    public void logout(ActionEvent event) throws IOException {
        Model.eraseSelf();
        switchToWelcomeScene(event);
    }

    public void switchToFollowersScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Followers.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToFollowingScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Following.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToWelcomeScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Welcome.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
