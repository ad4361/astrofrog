package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class SongSearchController {

    private Stage stage;
    private Scene scene;
    private Parent root;


    public TextField searchField;
    public Button goBackButton;

    public TableView<Song> songTable;
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

    public void switchtoPlaylistPage(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("PlaylistPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
