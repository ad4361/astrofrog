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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class SearchAllUsersController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label totalUsersLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<User> allUsersTable;
    @FXML
    private TableColumn<User,String> usernameColumn;
    @FXML
    private TableColumn<User,String> emailColumn;
    @FXML
    private TableColumn<User, String> actionColumn;

    ObservableList<User> allUsersList = FXCollections.observableArrayList();

    public void switchToFollowingScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Following.fxml"));
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

        String countUsersQuery = "SELECT COUNT(*) FROM \"User\"";

        String viewAllUsersQuery = "SELECT username, email FROM \"User\" WHERE username != ?";

        String viewWhoIFollowQuery = "SELECT username, email FROM \"User\" WHERE username IN " +
                "(SELECT \"userFollowed\" FROM \"Follows\" WHERE \"userFollowing\" = ?)";

        try {
            PreparedStatement stmt1 = PostgresSSH.connection.prepareStatement(countUsersQuery);
            ResultSet rsCount = stmt1.executeQuery();

            while(rsCount.next()) {
                int count = rsCount.getInt(1);
                totalUsersLabel.setText("Total users: " + count);
            }

            Set<String> whoIFollowSet = new HashSet<>();

            PreparedStatement stmt2 = PostgresSSH.connection.prepareStatement(viewWhoIFollowQuery);
            stmt2.setString(1, Model.self.getUsername());
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                String username = rs2.getString("username");
                whoIFollowSet.add(username);
            }


            PreparedStatement stmt3 = PostgresSSH.connection.prepareStatement(viewAllUsersQuery);
            stmt3.setString(1, Model.self.getUsername());
            ResultSet rs = stmt3.executeQuery();
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
                allUsersList.add(new User(username, email, button));
            }

            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("button"));

            allUsersTable.setItems(allUsersList);

            // filter the list
            FilteredList<User> filteredData = new FilteredList<>(allUsersList, b -> false);

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(User -> {

                    if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                        return false;
                    }

                    String searchKeyword = newValue.toLowerCase();

                    return User.getEmail().toLowerCase().contains(searchKeyword);
                });
            });

            SortedList<User> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(allUsersTable.comparatorProperty());
            allUsersTable.setItems(sortedData);

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

