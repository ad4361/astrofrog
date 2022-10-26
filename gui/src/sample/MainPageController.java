package sample;

import com.sun.webkit.Timer;
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
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;



    @FXML
    private Label hiLabel;
    @FXML
    private ChoiceBox<String> searchChoiceBox;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> songColumn;
    @FXML
    private TableColumn<Song, String> artistColumn;
    @FXML
    private TableColumn<Song, String> albumColumn;
    @FXML
    private TableColumn<Song, Integer> lengthColumn;
    @FXML
    private TableColumn<Song, Integer> listensColumn;
    @FXML
    private TableColumn<Song, String> genreColumn;
    @FXML
    private TableColumn<Song, String> actionColumn;

    ObservableList<Song> allSongsList = FXCollections.observableArrayList();
    String choice = "Song";

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

    public Song createSong(Integer songID) {

        // get song title, releasedate, length, artistName, and genreName
        String getTitleDateLengthArtistGenre = "SELECT S.title, S.releasedate, S.length, SG.\"genreName\", " +
                "SF.artname FROM \"Song\" S, \"SongGenre\" SG, \"SongFeat\" SF " +
                "WHERE S.songID = SG.\"songID\" AND SG.\"songID\" = SF.\"songID\" " +
                "AND S.songID = " + songID;
        String title = null;
        Date releasedate = null;
        int length = 0;
        String artistName = null;
        String genreName = null;

        try {
            ResultSet songInfo = PostgresSSH.executeSelect(getTitleDateLengthArtistGenre);
            while (songInfo.next()) {
                title = songInfo.getString("title");
                releasedate = songInfo.getDate("releasedate");
                length = songInfo.getInt("length");
                artistName = songInfo.getString("artname");
                genreName = songInfo.getString("genreName");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get album's name
        String getAlbumName = "select albumname FROM \"Album\" " +
                "WHERE albumid = (SELECT \"albumID\" from \"AlbumContains\" WHERE \"songID\" = " +
                songID + ")";
        String albumName = null;
        try {
            ResultSet albumInfo = PostgresSSH.executeSelect(getAlbumName);
            while (albumInfo.next()) {
                albumName = albumInfo.getString("albumname");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        }

        // now create Song
        return new Song(songID, title, releasedate, length, artistName, albumName, listenCount, genreName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hiLabel.setText("Hi, " + Model.self.getUsername() + "!");

        ObservableList<String> list = searchChoiceBox.getItems();
        list.addAll("Song", "Album", "Artist", "Genre");
        String[] choices = new String[] {"Song", "Album", "Artist", "Genre"};
        searchChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number old_val, Number new_val) -> {
                    this.choice = choices[new_val.intValue()];
                });


        String selectAllSongsQuery = "SELECT S.songID " +
                "FROM \"Song\" S, \"SongFeat\" SF " +
                "WHERE S.songID = SF.\"songID\" AND S.songID BETWEEN 1 AND 300" +
                "ORDER BY S.title, SF.artname";

        try {

            ResultSet rsAllSongs = PostgresSSH.executeSelect(selectAllSongsQuery);
            while (rsAllSongs.next()) {
                Integer songID = rsAllSongs.getInt("songID");
                //Button button;
                Song song = createSong(songID);
                allSongsList.add(song);
            }

            songColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            artistColumn.setCellValueFactory(new PropertyValueFactory<>("artistName"));
            albumColumn.setCellValueFactory(new PropertyValueFactory<>("albumName"));
            lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
            listensColumn.setCellValueFactory(new PropertyValueFactory<>("listenCount"));
            genreColumn.setCellValueFactory(new PropertyValueFactory<>("genreName"));
            //actionColumn.setCellValueFactory(new PropertyValueFactory<>("button"));

            songTable.setItems(allSongsList);

            // filter the list
            FilteredList<Song> filteredData = new FilteredList<>(allSongsList, b -> true);

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(Song -> {

                    if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                        return true;
                    }

                    String searchKeyword = newValue.toLowerCase();

                    if (this.choice.equals("Song")) {
                        if (Song.getTitle().toLowerCase().indexOf(searchKeyword) > -1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    else if (this.choice.equals("Artist")) {
                        if (Song.getArtistName().toLowerCase().indexOf(searchKeyword) > -1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    else if (this.choice.equals("Album")) {
                        if (Song.getAlbumName().toLowerCase().indexOf(searchKeyword) > -1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    else if (this.choice.equals("Genre")) {
                        if (Song.getGenreName().toLowerCase().indexOf(searchKeyword) > -1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                });
            });

            SortedList<Song> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(songTable.comparatorProperty());
            songTable.setItems(sortedData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
