package sample;

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
import java.sql.*;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class FollowersController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<User> followersTable;
    @FXML
    private TableColumn<User,String> usernameColumn;
    @FXML
    private TableColumn<User,String> emailColumn;
    @FXML
    private TableColumn<User, String> actionColumn;

    ObservableList<User> followersList = FXCollections.observableArrayList();

    public void switchToProfileScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ProfilePage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void unfollow(String userFollowing, String userFollowed) {

        String unfollowQuery = "DELETE FROM \"Follows\" WHERE \"userFollowing\"=? AND \"userFollowed\"=?";

        try {
            PreparedStatement statement = PostgresSSH.connection.prepareStatement(unfollowQuery);
            statement.setString(1, userFollowing);
            statement.setString(2, userFollowed);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void follow(String userFollowing, String userFollowed) {

        String followQuery = "INSERT INTO \"Follows\" VALUES (?, ?)";

        try {
            PreparedStatement statement = PostgresSSH.connection.prepareStatement(followQuery);
            statement.setString(1, userFollowing);
            statement.setString(2, userFollowed);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                PostgresSSH.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String viewFollowersQuery = "SELECT username, email FROM \"User\" WHERE username IN" +
                "(SELECT \"userFollowing\" FROM \"Follows\" WHERE \"userFollowed\" = ?)";

        String viewWhoIFollowQuery = "SELECT username, email FROM \"User\" WHERE username IN " +
                "(SELECT \"userFollowed\" FROM \"Follows\" WHERE \"userFollowing\" = ?)";

        try {
            Set<String> whoIFollowSet = new HashSet<>();

            PreparedStatement stmt = PostgresSSH.connection.prepareStatement(viewWhoIFollowQuery);
            stmt.setString(1, Model.self.getUsername());
            ResultSet rs2 = stmt.executeQuery();
            while (rs2.next()) {
                String username = rs2.getString("username");
                whoIFollowSet.add(username);
            }

            PreparedStatement statement = PostgresSSH.connection.prepareStatement(viewFollowersQuery);
            statement.setString(1, Model.self.getUsername());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                Button button;
                if (whoIFollowSet.contains(username)) {
                    button = new Button("Unfollow");
                    button.setOnAction((ActionEvent e) -> {
                        button.setText("Unfollowed X");
                        unfollow(Model.self.getUsername(), username);
                        button.setDisable(true);
                    } );
                } else {
                    button = new Button("Follow");
                    button.setOnAction((ActionEvent e) -> {
                        button.setText("Followed ✓");
                        follow(Model.self.getUsername(), username);
                        button.setDisable(true);
                    } );
                }
                followersList.add(new User(username, email, button));
            }

            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("button"));

            followersTable.setItems(followersList);

            // filter the list
            FilteredList<User> filteredData = new FilteredList<>(followersList, b -> true);

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(User -> {

                    if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                        return true;
                    }

                    String searchKeyword = newValue.toLowerCase();

                    return User.getEmail().toLowerCase().contains(searchKeyword);
                });
            });

            SortedList<User> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(followersTable.comparatorProperty());
            followersTable.setItems(sortedData);

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
