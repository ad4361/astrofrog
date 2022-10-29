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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class RemoveAlbumController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label plNameLabel;
    @FXML
    private ChoiceBox<String> sortOrderChoiceBox;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Album> albumTable;
    @FXML
    private TableColumn<Album, String> artistColumn;
    @FXML
    private TableColumn<Album, String> albumColumn;
    @FXML
    private TableColumn<Album, String> genreColumn;
    @FXML
    private TableColumn<Album, String> actionColumn;

    ObservableList<Album> allAlbumsList = FXCollections.observableArrayList();
    String sortOrderChoice = "ASC";
    String query = "SELECT albumID FROM \"Album\" " +
            "WHERE albumid IN (SELECT DISTINCT \"albumID\" FROM \"AlbumContains\" " +
            "WHERE \"songID\" BETWEEN 1 AND 300) " +
            "ORDER BY albumname " + this.sortOrderChoice;

    public void switchToPLDetailsScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("PLDetails.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public Album createAlbum(Integer albumID, String username) {

        // get albumname
        String getAlbumArtistGenreQuery = "SELECT A.albumname, AF.\"artistName\", AG.genre " +
                "FROM \"Album\" A, \"AlbumFeat\" AF, \"AlbumGenre\" AG " +
                "WHERE A.albumid = AF.albumid AND AF.albumid = AG.albumid " +
                "AND A.albumid = " + albumID;
        String albumname = "";
        String artistName = "";
        String genre = "";

        try {
            ResultSet albumInfo = PostgresSSH.executeSelect(getAlbumArtistGenreQuery);
            while (albumInfo.next()) {
                albumname = albumInfo.getString("albumname");
                artistName = albumInfo.getString("artistName");
                genre = albumInfo.getString("genre");
            }
        } catch(Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        // create add button
        Button button = new Button("❌");
        button.setOnAction((ActionEvent e) -> {

            Set<Integer> songIDsToDelete = new HashSet<>();

            // get all songs from the album that ARE in the playlist
            String getAlbumSongsQuery = "SELECT \"songID\" FROM \"AlbumContains\" " +
                    "WHERE \"albumID\" = " + albumID +
                    " AND \"songID\" IN " +
                    "(SELECT \"songID\" FROM \"PLContains\" WHERE \"plNAME\" = '" + Model.PLname +
                    "' AND username = '" + username + "')";


            try {
                int songID;
                ResultSet rs = PostgresSSH.executeSelect(getAlbumSongsQuery);
                while (rs.next()) {
                    songID = rs.getInt(1);
                    songIDsToDelete.add(songID);
                }

                for (int ID : songIDsToDelete) {
                    String deleteSongQuery = "DELETE FROM \"PLContains\" WHERE \"plNAME\" = '"
                            + Model.PLname + "' AND username = '" + username +
                            "' AND \"songID\" = " + ID;
                    Statement st = PostgresSSH.connection.createStatement();
                    st.executeUpdate(deleteSongQuery);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                try {
                    PostgresSSH.connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            button.setDisable(true);
        } );

        // now create Album
        return new Album(albumID, albumname, artistName, genre, button);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        plNameLabel.setText(Model.PLname);

        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("albumname"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("button"));
        ObservableList<String> sortOrderList = sortOrderChoiceBox.getItems();
        sortOrderList.addAll("Ascending", "Descending");
        String[] sortOrderChoices = new String[] {"ASC", "DESC"};
        sortOrderChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> observable, Number old_val, Number new_val) -> {
                    this.sortOrderChoice = sortOrderChoices[new_val.intValue()];
                    this.query = "SELECT albumID FROM \"Album\" " +
                            "WHERE albumid IN (SELECT DISTINCT \"albumID\" FROM \"AlbumContains\" " +
                            "WHERE \"songID\" BETWEEN 1 AND 300) " +
                            "ORDER BY albumname " + this.sortOrderChoice;
                    allAlbumsList.clear();
                    runQuery(query);
                });
        runQuery(query);
    }


    public void runQuery(String query) {
        try {

            ResultSet rsAllAlbums = PostgresSSH.executeSelect(query);
            while (rsAllAlbums.next()) {
                Integer albumID = rsAllAlbums.getInt(1);
                Album album = createAlbum(albumID, Model.self.getUsername());
                allAlbumsList.add(album);
            }

            albumTable.setItems(allAlbumsList);

            // filter the list
            FilteredList<Album> filteredData = new FilteredList<>(allAlbumsList, b -> true);
            filteredData.addListener((ListChangeListener.Change<? extends Album> change) -> albumTable.refresh());

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(Album -> {

                    if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                        return true;
                    }

                    String searchKeyword = newValue.toLowerCase();

                    return Album.getAlbumname().toLowerCase().contains(searchKeyword);

                });
            });

            SortedList<Album> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(albumTable.comparatorProperty());
            albumTable.setItems(sortedData);

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

