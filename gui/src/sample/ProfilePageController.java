package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfilePageController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label usernameLabel;
    @FXML
    private Label playlistLabel;
    @FXML
    private Label followersLabel;
    @FXML
    private Label followingLabel;


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

    public void switchToPlaylistScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("PlaylistPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToMainPageScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameLabel.setText(Model.self.getUsername());

        String countPlaylists = "SELECT COUNT(*) FROM \"Playlist\" WHERE username = ?";
        String countFollowers = "SELECT COUNT(*) FROM \"Follows\" WHERE \"userFollowed\" = ?";
        String countFollowing = "SELECT COUNT(*) FROM \"Follows\" WHERE \"userFollowing\" = ?";

        int playlists;
        int followers;
        int following;

        try {
            PreparedStatement stmt1 = PostgresSSH.connection.prepareStatement(countPlaylists);
            stmt1.setString(1, Model.self.getUsername());
            ResultSet rsPlaylist = stmt1.executeQuery();
            while(rsPlaylist.next()) {
                playlists = rsPlaylist.getInt(1);
                playlistLabel.setText("Playlists: " + playlists);
            }

            PreparedStatement stmt2 = PostgresSSH.connection.prepareStatement(countFollowers);
            stmt2.setString(1, Model.self.getUsername());
            ResultSet rsFollowers = stmt2.executeQuery();
            while(rsFollowers.next()) {
                followers = rsFollowers.getInt(1);
                followersLabel.setText("Followers: " + followers);
            }

            PreparedStatement stmt3 = PostgresSSH.connection.prepareStatement(countFollowing);
            stmt3.setString(1, Model.self.getUsername());
            ResultSet rsFollowing = stmt3.executeQuery();
            while(rsFollowing.next()) {
                following = rsFollowing.getInt(1);
                followingLabel.setText("Following: " + following);
            }

        } catch(Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
