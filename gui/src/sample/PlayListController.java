package sample;

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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Set;

public class PlayListController implements Initializable {

    public Button BackButton;
    public TextField newPLname;
    public Button createButton;
    public Label createMessageLabel;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public Label playlistOwner;

    @FXML
    private TableView<PlayList> playlistTable;
    @FXML
    private TableColumn<PlayList, String> nameColumn;
    @FXML
    private TableColumn<PlayList, String> lenColumn;
    @FXML
    private TableColumn<PlayList, Integer> numColumn;
    @FXML
    private TableColumn<PlayList, String> viewColumn;

    ObservableList<PlayList> allPlayLists = FXCollections.observableArrayList();
    Set<String> existingPLnames;


    public void switchToMainPage(ActionEvent event) throws IOException  {
        root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToProfileScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ProfilePage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToPLDetailsScene(ActionEvent event) throws IOException  {
        root = FXMLLoader.load(getClass().getResource("PLDetails.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public PlayList createPlaylist(String plname, String username) {

        int songTotal = 0;
        int totalDuration = 0;

        // get number of songs in playlist
        String getNum = "SELECT COUNT(*) FROM \"PLContains\" WHERE \"username\" = '" +
                username + "' AND \"plNAME\"= '" + plname + "' AND \"songID\" BETWEEN 1 AND 300";
        try {
            ResultSet plNum = PostgresSSH.executeSelect(getNum);
            while (plNum.next()) {
                songTotal = plNum.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        // get playlist length
        String getLength = "SELECT SUM(length) FROM \"Song\" WHERE songID IN " +
                "(SELECT \"songID\" FROM \"PLContains\" WHERE \"plNAME\" = '" + plname +
                "' AND username = '" + username + "' AND \"songID\" BETWEEN 1 AND 300)";
        try {
            ResultSet plLen = PostgresSSH.executeSelect(getLength);
            while (plLen.next()) {
                totalDuration = plLen.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        // convert seconds to minutes
        String length = null;
        int minutes = totalDuration / 60;
        int seconds = (int) (totalDuration -(minutes*60));
        if(seconds <= 9){
            length = minutes + ":0" + seconds;
        } else{
            length = minutes + ":" + seconds;
        }

        // create view button
        Button button = new Button("View");
        button.setOnAction((ActionEvent event) -> {
            Model.setPlname(plname);
            try {
                switchToPLDetailsScene(event);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    PostgresSSH.connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        // now create Playlist
        return new PlayList(plname, username, length, songTotal, button);
    }

    public void makePlaylist(ActionEvent actionEvent) throws IOException {

        if (newPLname.getText().isBlank()) {
            createMessageLabel.setText("Please enter a playlist name");
        }
        else if (existingPLnames.contains(newPLname.getText())) {
            createMessageLabel.setText("You already have a playlist with that name");
        } else {
            String newPLQuery = "INSERT INTO \"Playlist\" VALUES ('" + newPLname.getText() + "', '"
                    + Model.self.getUsername() + "')";
            try {
                Statement st = PostgresSSH.connection.createStatement();
                st.executeUpdate(newPLQuery);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    PostgresSSH.connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            // create view button
            Button button = new Button("View");
            button.setOnAction((ActionEvent event) -> {
                Model.setPlname(newPLname.getText());
                try {
                    switchToPLDetailsScene(event);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        PostgresSSH.connection.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            });
//            PlayList newPlayList = new PlayList(newPLname.getText(), Model.self.getUsername(),
//                    "0", 0, button);
            Model.setPlname(newPLname.getText());
            switchToPLDetailsScene(actionEvent);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playlistOwner.setText(Model.self.getUsername() + "'s playlists");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("plname"));
        lenColumn.setCellValueFactory(new PropertyValueFactory<>("totalDuration"));
        numColumn.setCellValueFactory(new PropertyValueFactory<>("totalSongs"));
        viewColumn.setCellValueFactory(new PropertyValueFactory<>("button"));

        existingPLnames = new HashSet<>();

        String getPlaylistsQuery = "SELECT plname FROM \"Playlist\" WHERE username = '" +
                Model.self.getUsername() + "' ORDER BY plname ASC";
        String plname = null;

        try {
            ResultSet rs = PostgresSSH.executeSelect(getPlaylistsQuery);
            while (rs.next()) {
                plname = rs.getString("plname");
                existingPLnames.add(plname);
                PlayList playList = createPlaylist(plname, Model.self.getUsername());
                allPlayLists.add(playList);
            }
        } catch(Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        playlistTable.setItems(allPlayLists);
    }
}
