package sample;

import com.gluonhq.charm.glisten.control.TextField;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class PlayListController implements Initializable {

    public Button BackButton;
    public Button searchButton;
    public TextField newPLname;
    public Button createButton;
    public Label createMessageLabel;
    private Stage stage;
    private Scene scene;
    private Parent root;

    public Label playlistOwner;
    public Button friendButton;
    private TableView tableview;

    public void switchToMainPage(ActionEvent event) throws IOException  {
        root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToFollowerSelectScene(ActionEvent actionEvent) {
    }

    public void switchToSongSearchScene(ActionEvent actionEvent) {
    }

    public void makePlaylist(ActionEvent actionEvent) {

        if(newPLname.getText().isBlank()){
            createMessageLabel.setText("Please enter a playlist name");
        }else{
            System.out.println("making playlist " + newPLname.getText());
            String newPLQuery = "INSERT INTO \"Playlist\" VALUES ('" + newPLname.getText() + "', '" + Model.self.getUsername() + "')";
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playlistOwner.setText(Model.self.getUsername() + "'s playlists");
        //System.out.println(newPLname.toString());

        //Query for user's playlists
    }


}
