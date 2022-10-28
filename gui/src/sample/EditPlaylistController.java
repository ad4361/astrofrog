package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class EditPlaylistController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label plNameLabel;
    @FXML
    private Label warningMessageLabel;
    @FXML
    private TextField newNameField;

    public void switchToPLDetailsScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("PLDetails.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void changeName(ActionEvent event) {
        Set<String> existingPLnames = new HashSet<>();

        String getPlaylistsQuery = "SELECT plname FROM \"Playlist\" WHERE username = '" +
                Model.self.getUsername() + "' ORDER BY plname ASC";
        String plname = null;

        try {
            ResultSet rs = PostgresSSH.executeSelect(getPlaylistsQuery);
            while (rs.next()) {
                plname = rs.getString("plname");
                existingPLnames.add(plname);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (newNameField.getText().isBlank()) {
            warningMessageLabel.setText("Please enter a playlist name");
        }
        else if (existingPLnames.contains(newNameField.getText())) {
            warningMessageLabel.setText("You already have a playlist with that name");
        } else {
            String renameQuery = "UPDATE \"Playlist\" " +
                    "SET plname = '" + newNameField.getText() + "' " +
                    "WHERE plname = '" + Model.PLname + "' AND username = '" +
                     Model.self.getUsername() + "'";
            try {
                Statement st = PostgresSSH.connection.createStatement();
                st.executeUpdate(renameQuery);
                Model.setPlname(newNameField.getText());
                switchToPLDetailsScene(event);
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        plNameLabel.setText(Model.PLname);
    }
}
