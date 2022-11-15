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
import javafx.scene.control.ListView;
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
    ListView<String> genreListView;

    /** */
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

    ObservableList<Song> top50List = FXCollections.observableArrayList();

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

    ObservableList<Song> playHistoryList = FXCollections.observableArrayList();


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

        // top 5 genres of the month
        String top5GenresQuery = "SELECT ETC.name, COUNT(*) FROM " +
                "(SELECT US.\"songID\", G.name " +
                "FROM \"UserSong\" US, \"SongGenre\" SG, \"Genre\" G " +
                "WHERE EXTRACT(YEAR FROM \"timeListened\") = EXTRACT(YEAR FROM now()) " +
                "AND EXTRACT(MONTH FROM \"timeListened\") = EXTRACT(MONTH FROM now()) " +
                "AND US.\"songID\" = SG.\"songID\" AND SG.\"genreID\" = G.\"genreID\") AS ETC " +
                "GROUP BY 1 " +
                "ORDER BY 2 DESC " +
                "LIMIT 5";
        try {
            PreparedStatement stmtGenre = PostgresSSH.connection.prepareStatement(top5GenresQuery);
            ResultSet rsTop5Genre = stmtGenre.executeQuery();
            int i = 1;
            while (rsTop5Genre.next()) {
                String genreName = rsTop5Genre.getString(1);
                genreListView.getItems().add(i + ". " + genreName);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        // top 50 songs of the last 30 days : 1
        songColumn1.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn1.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        listensColumn1.setCellValueFactory(new PropertyValueFactory<>("listenCount"));
        genreColumn1.setCellValueFactory(new PropertyValueFactory<>("genreName"));
        playColumn1.setCellValueFactory(new PropertyValueFactory<>("button"));

        String last30DaysQuery = "SELECT \"songID\", COUNT(*) " +
                "FROM \"UserSong\" " +
                "WHERE \"timeListened\" > now() - interval '30 day' " +
                "GROUP BY 1 " +
                "ORDER BY 2 DESC " +
                "LIMIT 50";
        try {
            PreparedStatement stmt1 = PostgresSSH.connection.prepareStatement(last30DaysQuery);
            ResultSet rs30Days = stmt1.executeQuery();
            while (rs30Days.next()) {
                Integer songID = rs30Days.getInt("songID");
                Song song = createSong(songID, Model.self.getUsername());
                top50List.add(song);
            }
            top50TableView.setItems(top50List);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


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

        // song recs based on play history and similar users
        songColumn3.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn3.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        listensColumn3.setCellValueFactory(new PropertyValueFactory<>("listenCount"));
        genreColumn3.setCellValueFactory(new PropertyValueFactory<>("genreName"));
        playColumn3.setCellValueFactory(new PropertyValueFactory<>("button"));

        String playHistoryQuery = "SELECT BIGETC.\"songID\" FROM " +
                "((SELECT \"songID\" FROM \"SongGenre\" " +
                "WHERE \"genreID\" IN (SELECT GENRES.\"genreID\" FROM " +
                "(SELECT ETC.\"genreID\", COUNT(*) FROM " +
                "(SELECT US.\"songID\", G.\"genreID\" " +
                "FROM \"UserSong\" US, \"SongGenre\" SG, \"Genre\" G " +
                "WHERE US.username = ? " +
                "AND US.\"songID\" = SG.\"songID\" AND SG.\"genreID\" = G.\"genreID\") AS ETC " +
                "GROUP BY 1 " +
                "ORDER BY 2 DESC " +
                "LIMIT 6) " +
                "AS GENRES)) " +
                "UNION " +
                "(SELECT \"songID\" FROM \"SongFeat\" " +
                "WHERE artname = ? OR artname = ? OR artname = ?) " +
                "UNION " +
                "(SELECT DISTINCT \"songID\" FROM \"UserSong\" " +
                "WHERE username IN (SELECT DISTINCT sIo.other FROM (SELECT sImo.other AS other " +
                "FROM (SELECT DISTINCT ME.\"songID\", ME.username AS me, OTHER.username AS other " +
                "FROM \"UserSong\" ME,  \"UserSong\" OTHER " +
                "WHERE ME.\"songID\" = OTHER.\"songID\" " +
                "AND ME.username != OTHER.username " +
                "AND ME.username = ? " +
                "ORDER BY 3) as sImo " +
                "ORDER BY random() " +
                "LIMIT 5) AS sIo))) AS BIGETC " +
                "ORDER BY random() " +
                "LIMIT 40";
        try {
            PreparedStatement stmt3 = PostgresSSH.connection.prepareStatement(playHistoryQuery);
            stmt3.setString(1, Model.self.getUsername());
            stmt3.setString(2, Model.top3Artists.get(0));
            stmt3.setString(3, Model.top3Artists.get(1));
            stmt3.setString(4, Model.top3Artists.get(2));
            stmt3.setString(5, Model.self.getUsername());
            ResultSet rsRecSongs = stmt3.executeQuery();
            while (rsRecSongs.next()) {
                Integer songID = rsRecSongs.getInt("songID");
                Song song = createSong(songID, Model.self.getUsername());
                playHistoryList.add(song);
            }
            playHistoryTableView.setItems(playHistoryList);

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
