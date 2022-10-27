package sample;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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

public class PLDetailsController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label plNameLabel;
    @FXML
    private ChoiceBox<String> searchChoiceBox;
    @FXML
    private ChoiceBox<String> sortByChoiceBox;
    @FXML
    private ChoiceBox<String> sortOrderChoiceBox;
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
    String sortChoice = "Song";
    String sortOrderChoice = "ASC";
    String query = "SELECT S.songid " +
            "FROM \"Song\" S, \"SongFeat\" SF " +
            "WHERE S.songID = SF.\"songID\" AND S.songID BETWEEN 1 AND 300 " +
            "AND S.songid IN " +
            "(SELECT \"songID\" FROM \"PLContains\" " +
            "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
            Model.self.getUsername() + "')" +
            "ORDER BY S.title, SF.artname ASC;";

    public void switchtoPlaylistPage(ActionEvent event) throws IOException {
        Model.erasePLname();
        root = FXMLLoader.load(getClass().getResource("PlaylistPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public Song createSong(Integer songID, String username) {

        // get song title, releasedate, length, artistName, and genreName
        String getTitleDateLengthArtistGenre = "SELECT S.title, S.releasedate, S.length, SG.\"genreName\", " +
                "SF.artname FROM \"Song\" S, \"SongGenre\" SG, \"SongFeat\" SF " +
                "WHERE S.songID = SG.\"songID\" AND SG.\"songID\" = SF.\"songID\" " +
                "AND S.songID = " + songID;
        String title = null;
        Date releasedate = null;
        int ilength = 0;
        String length = null;
        String artistName = null;
        String genreName = null;

        try {
            ResultSet songInfo = PostgresSSH.executeSelect(getTitleDateLengthArtistGenre);
            while (songInfo.next()) {
                title = songInfo.getString("title");
                releasedate = songInfo.getDate("releasedate");
                ilength = songInfo.getInt("length");
                artistName = songInfo.getString("artname");
                genreName = songInfo.getString("genreName");
            }
            int minutes = ilength / 60;
            int seconds = (int) (ilength - (minutes * 60));
            if (seconds <= 9) {
                length = minutes + ":0" + seconds;
            } else {
                length = minutes + ":" + seconds;
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

        Button button = new Button("❌");
        button.setOnAction((ActionEvent e) -> {
            // DELETE FROM "PLContains"
            //WHERE "plNAME" = '' AND username = '' AND "songID" = 1;

            String removeQuery = "DELETE FROM \"PLContains\" WHERE \"plNAME\" = '" + Model.PLname +
                    "' AND username = '" + username + "' AND \"songID\" = " + songID;
            try {
                Statement st = PostgresSSH.connection.createStatement();
                st.executeUpdate(removeQuery);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            button.setDisable(true);
        });

        // now create Song
        return new Song(songID, title, releasedate, length, artistName,
                albumName, listenCount, genreName, button);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        plNameLabel.setText(Model.PLname);

        songColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("albumName"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        listensColumn.setCellValueFactory(new PropertyValueFactory<>("listenCount"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genreName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("button"));

        ObservableList<String> list = searchChoiceBox.getItems();
        list.addAll("Song", "Album", "Artist", "Genre");
        String[] choices = new String[] {"Song", "Album", "Artist", "Genre"};
        searchChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number old_val, Number new_val) -> {
                    this.choice = choices[new_val.intValue()];
                });

        ObservableList<String> sortByList = sortByChoiceBox.getItems();
        sortByList.addAll("Song", "Artist", "Genre", "Year");
        String[] sortChoices = new String[] {"Song", "Artist", "Genre", "Year"};
        sortByChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number old_val, Number new_val) -> {
                    this.sortChoice = sortChoices[new_val.intValue()];
                    if (this.sortChoice.equals("Song")) {
                        this.query = "SELECT S.songid " +
                                "FROM \"Song\" S, \"SongFeat\" SF " +
                                "WHERE S.songID = SF.\"songID\" AND S.songID BETWEEN 1 AND 300 " +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY S.title " + this.sortOrderChoice + ", SF.artname ASC";
                        allSongsList.clear();
                        runQuery(query);
                    } else if (this.sortChoice.equals("Year")) {
                        this.query = "SELECT S.songID " +
                                "FROM \"Song\" S " +
                                "WHERE S.songID BETWEEN 1 AND 300" +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY S.releasedate " + this.sortOrderChoice;
                        allSongsList.clear();
                        runQuery(query);
                    } else if (this.sortChoice.equals("Artist")) {
                        this.query = "SELECT S.songID " +
                                "FROM \"Song\" S, \"SongFeat\" SF " +
                                "WHERE S.songID = SF.\"songID\" AND S.songID BETWEEN 1 AND 300" +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY  SF.artname " + this.sortOrderChoice;
                        allSongsList.clear();
                        runQuery(query);
                    } else if (this.sortChoice.equals("Genre")) {
                        this.query = "SELECT S.songID " +
                                "FROM \"Song\" S, \"SongGenre\" SG " +
                                "WHERE S.songID = SG.\"songID\" AND S.songID BETWEEN 1 AND 300" +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY  SG.\"genreName\" " + this.sortOrderChoice;
                        allSongsList.clear();
                        runQuery(query);
                    }
                });

        ObservableList<String> sortOrderList = sortOrderChoiceBox.getItems();
        sortOrderList.addAll("Ascending", "Descending");
        String[] sortOrderChoices = new String[] {"ASC", "DESC"};
        sortOrderChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number old_val, Number new_val) -> {
                    this.sortOrderChoice = sortOrderChoices[new_val.intValue()];
                    if (this.sortChoice.equals("Song")) {
                        this.query = "SELECT S.songid " +
                                "FROM \"Song\" S, \"SongFeat\" SF " +
                                "WHERE S.songID = SF.\"songID\" AND S.songID BETWEEN 1 AND 300 " +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY S.title " + this.sortOrderChoice + ", SF.artname ASC";
                        allSongsList.clear();
                        runQuery(query);
                    } else if (this.sortChoice.equals("Year")) {
                        this.query = "SELECT S.songID " +
                                "FROM \"Song\" S " +
                                "WHERE S.songID BETWEEN 1 AND 300" +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY S.releasedate " + this.sortOrderChoice;
                        allSongsList.clear();
                        runQuery(query);
                    } else if (this.sortChoice.equals("Artist")) {
                        this.query = "SELECT S.songID " +
                                "FROM \"Song\" S, \"SongFeat\" SF " +
                                "WHERE S.songID = SF.\"songID\" AND S.songID BETWEEN 1 AND 300" +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY  SF.artname " + this.sortOrderChoice;
                        allSongsList.clear();
                        runQuery(query);
                    } else if (this.sortChoice.equals("Genre")) {
                        this.query = "SELECT S.songID " +
                                "FROM \"Song\" S, \"SongGenre\" SG " +
                                "WHERE S.songID = SG.\"songID\" AND S.songID BETWEEN 1 AND 300" +
                                "AND S.songid IN " +
                                "(SELECT \"songID\" FROM \"PLContains\" " +
                                "WHERE \"plNAME\" = '" + Model.PLname + "' AND \"username\" = '" +
                                Model.self.getUsername() + "')" +
                                "ORDER BY  SG.\"genreName\" " + this.sortOrderChoice;
                        allSongsList.clear();
                        runQuery(query);
                    }
                });

        runQuery(this.query);
    }

    public void runQuery(String query) {
        try {
            ResultSet rsAllSongs = PostgresSSH.executeSelect(query);
            while (rsAllSongs.next()) {
                Integer songID = rsAllSongs.getInt("songID");
                Song song = createSong(songID, Model.self.getUsername());
                allSongsList.add(song);
            }

            songTable.setItems(allSongsList);

            // filter the list
            FilteredList<Song> filteredData = new FilteredList<>(allSongsList, b -> true);
            filteredData.addListener((ListChangeListener.Change<? extends Song> change) -> songTable.refresh());

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(Song -> {

                    if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                        return true;
                    }

                    String searchKeyword = newValue.toLowerCase();

                    return switch (this.choice) {
                        case "Song" -> Song.getTitle().toLowerCase().contains(searchKeyword);
                        case "Artist" -> Song.getArtistName().toLowerCase().contains(searchKeyword);
                        case "Album" -> Song.getAlbumName().toLowerCase().contains(searchKeyword);
                        case "Genre" -> Song.getGenreName().toLowerCase().contains(searchKeyword);
                        default -> false;
                    };
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
