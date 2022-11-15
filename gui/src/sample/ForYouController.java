package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ForYouController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    TableView<Song> topGenresTableView;
    @FXML
    TableColumn<Song, String> genreTableColumn;

    @FXML
    TableView<Song> top50TableView;
    @FXML
    TableColumn<Song, String> songColumn1;
    @FXML
    TableColumn<Song, String> artistColumn1;
    @FXML
    TableColumn<Song, String> genreColumn1;
    @FXML
    TableColumn<Song, Integer> listensColumn1;
    @FXML
    TableColumn<Song, String> playColumn1;

    /** */
    @FXML
    TableView<Song> friendSongsTableView;
    @FXML
    TableColumn<Song, String> songColumn2;
    @FXML
    TableColumn<Song, String> artistColumn2;
    @FXML
    TableColumn<Song, String> genreColumn2;
    @FXML
    TableColumn<Song, Integer> listensColumn2;
    @FXML
    TableColumn<Song, String> playColumn2;

    ObservableList<Song> friendSongsList = FXCollections.observableArrayList();

    /** */
    @FXML
    TableView<Song> playHistoryTableView;
    @FXML
    TableColumn<Song, String> songColumn3;
    @FXML
    TableColumn<Song, String> artistColumn3;
    @FXML
    TableColumn<Song, String> genreColumn3;
    @FXML
    TableColumn<Song, Integer> listensColumn3;
    @FXML
    TableColumn<Song, String> playColumn3;



    public void switchToProfileScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ProfilePage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public Song createSong(Integer songID, String username) {

        // get song title, artistName, and genreName
        String getTitleArtistGenre = "SELECT S.title, G.name, " +
                "SF.artname FROM \"Song\" S, \"SongGenre\" SG, \"SongFeat\" SF, \"Genre\" G " +
                "WHERE S.songID = SG.\"songID\" AND SG.\"songID\" = SF.\"songID\" AND SG.\"genreID\" = G.\"genreID\" " +
                "AND S.songID = " + songID;
        String title = null;
        String artistName = null;
        String genreName = null;

        try {
            ResultSet songInfo = PostgresSSH.executeSelect(getTitleArtistGenre);
            while (songInfo.next()) {
                title = songInfo.getString("title");
                genreName = songInfo.getString("name");
                artistName = songInfo.getString("artname");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        // get listen count
        String getListens = "SELECT COUNT(*) FROM \"UserSong\" WHERE \"songID\" = " + songID;
        int listenCount = 0;
        try {
            ResultSet listenInfo = PostgresSSH.executeSelect(getListens);
            while (listenInfo.next()) {
                listenCount = listenInfo.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
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
                try {
                    PostgresSSH.connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } );

        // now create Song
        return new Song(songID, title, artistName, listenCount, genreName, button);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // top 50 songs among friends: 2
        songColumn2.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn2.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        listensColumn2.setCellValueFactory(new PropertyValueFactory<>("listenCount"));
        genreColumn2.setCellValueFactory(new PropertyValueFactory<>("genreName"));
        playColumn2.setCellValueFactory(new PropertyValueFactory<>("button"));

        String top50FriendsQuery = "SELECT \"songID\", COUNT(*) FROM \"UserSong\" " +
                "WHERE username IN (SELECT \"userFollowed\" FROM \"Follows\" " +
                "WHERE \"userFollowing\" = ?) " +
                "GROUP BY 1 " +
                "ORDER BY 2 DESC " +
                "LIMIT 50";

        try {
            PreparedStatement stmt2 = PostgresSSH.connection.prepareStatement(top50FriendsQuery);
            stmt2.setString(1, Model.self.getUsername());
            ResultSet rsFriendSongs = stmt2.executeQuery();
            while (rsFriendSongs.next()) {
                Integer songID = rsFriendSongs.getInt("songID");
                Song song = createSong(songID, Model.self.getUsername());
                friendSongsList.add(song);
            }
            friendSongsTableView.setItems(friendSongsList);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
