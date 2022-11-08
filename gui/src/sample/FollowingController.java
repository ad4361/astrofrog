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
import java.util.ResourceBundle;

public class FollowingController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<User> followingTable;
    @FXML
    private TableColumn<User,String> usernameColumn;
    @FXML
    private TableColumn<User,String> emailColumn;
    @FXML
    private TableColumn<User, String> actionColumn;

    ObservableList<User> followingList = FXCollections.observableArrayList();

    public void switchToProfileScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ProfilePage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToSearchAllUsersScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("SearchAllUsers.fxml"));
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String viewFollowingQuery = "SELECT username, email FROM \"User\" WHERE username IN " +
                "(SELECT \"userFollowed\" FROM \"Follows\" WHERE \"userFollowing\" = ?)";

        try {
            PreparedStatement stmt = PostgresSSH.connection.prepareStatement(viewFollowingQuery);
            stmt.setString(1, Model.self.getUsername());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                Button button = new Button("Unfollow");
                button.setOnAction((ActionEvent e) -> {
                    button.setText("Unfollowed X");
                    unfollow(Model.self.getUsername(), username);
                    button.setDisable(true);
                } );
                followingList.add(new User(username, email, button));
            }

            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("button"));

            followingTable.setItems(followingList);

            // filter the list
            FilteredList<User> filteredData = new FilteredList<>(followingList, b -> true);

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
            sortedData.comparatorProperty().bind(followingTable.comparatorProperty());
            followingTable.setItems(sortedData);

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
