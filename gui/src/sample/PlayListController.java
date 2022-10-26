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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

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

    public void switchToSongSearchScene(ActionEvent event) throws IOException  {
        root = FXMLLoader.load(getClass().getResource("SongSearch.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public PlayList createPlaylist(Integer songID, String username) {
        // get playlist name
        String getPLName = "SELECT plname FROM \"Playlist\" WHERE username = '" + Model.self.getUsername() + "'";
        String plname = null;
        int songTotal = 0;
        int totalDuration = 0;

        try {
            ResultSet playlistInfo = PostgresSSH.executeSelect(getPLName);
            while (playlistInfo.next()) {
                plname = playlistInfo.getString("plname");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get playlist cardinality
        String getNum = "SELECT COUNT(*) FROM \"PLContains\" WHERE \"username\" = '" + Model.self.getUsername() +
                "' AND \"plNAME\"= '" + plname + "'";
        try {
            ResultSet plNum = PostgresSSH.executeSelect(getNum);
            while (plNum.next()) {
                songTotal = plNum.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button button = new Button("▷");
        button.setOnAction((ActionEvent e) -> {
            //INSERT INTO "UserSong" VALUES('fsowley5m', 774, now());

            String listenQuery = "INSERT INTO \"UserSong\" VALUES('" + username + "', " +
                    songID + ", '" + LocalDateTime.now() + "')";
            try {
                Statement st = PostgresSSH.connection.createStatement();
                st.executeUpdate(listenQuery);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } );

        // now create Playlist
        return new PlayList(plname, Model.self.getUsername(), totalDuration, songTotal);
    }

    public void makePlaylist(ActionEvent actionEvent) {

        if(newPLname.getText().isBlank()){
            createMessageLabel.setText("Please enter a playlist name");
        } else{
            System.out.println("making playlist " + newPLname.getText());
            String newPLQuery = "INSERT INTO \"Playlist\" VALUES ('" + newPLname.getText() + "', '" + Model.self.getUsername() + "')";
            try {
                Statement st = PostgresSSH.connection.createStatement();
                st.executeUpdate(newPLQuery);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playlistOwner.setText(Model.self.getUsername() + "'s playlists");
        //System.out.println(newPLname.toString());

        //Query for user's playlists
    }


}
