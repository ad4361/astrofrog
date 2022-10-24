package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;

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
    public void logout(ActionEvent event) {

    }

}
