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
    private Label welcomeText;

    @FXML
    public void initialize() {
        //TODO
        /*
            run a query to find username and set welcometext to username

            do the same for the rest of the information we care about

            make
         */

        welcomeText.setText("Morb");
    }

    @FXML
    public void logout(ActionEvent event) {

    }

}
