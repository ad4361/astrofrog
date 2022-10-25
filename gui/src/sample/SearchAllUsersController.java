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
import java.sql.ResultSet;
import java.sql.Statement;
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

        String unfollowQuery = "DELETE FROM \"Follows\" WHERE \"userFollowing\" = '" +
                userFollowing + "' AND \"userFollowed\" = '" + userFollowed + "'";

        try {
            Statement st = PostgresSSH.connection.createStatement();
            st.executeUpdate(unfollowQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void follow(String userFollowing, String userFollowed) {

        String followQuery = "INSERT INTO \"Follows\" VALUES ('" + userFollowing + "', '" +
                userFollowed + "')";

        try {
            Statement st = PostgresSSH.connection.createStatement();
            st.executeUpdate(followQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String countUsersQuery = "SELECT COUNT(*) FROM \"User\"";

        String viewAllUsersQuery = "SELECT username, email FROM \"User\"";

        String viewWhoIFollowQuery = "SELECT username FROM \"User\" WHERE username IN" +
                "(SELECT \"userFollowed\" FROM \"Follows\" WHERE \"userFollowing\" = '" +
                Model.self.getUsername() + "')";

        try {

            ResultSet rsCount = PostgresSSH.executeSelect(countUsersQuery);
            while(rsCount.next()) {
                Integer count = rsCount.getInt(1);
                totalUsersLabel.setText("Total users: " + count);
            }

            Set<String> whoIFollowSet = new HashSet<>();

            ResultSet rs2 = PostgresSSH.executeSelect(viewWhoIFollowQuery);
            while (rs2.next()) {
                String username = rs2.getString("username");
                whoIFollowSet.add(username);
            }

            ResultSet rs = PostgresSSH.executeSelect(viewAllUsersQuery);
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
            FilteredList<User> filteredData = new FilteredList<>(allUsersList, b -> true);

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(User -> {

                    if (newValue.isEmpty() || newValue.isBlank() || newValue == null) {
                        return true;
                    }

                    String searchKeyword = newValue.toLowerCase();

                    if (User.getEmail().toLowerCase().indexOf(searchKeyword) > -1) {
                        return true;
                    } else {
                        return false;
                    }
                });
            });

            SortedList<User> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(allUsersTable.comparatorProperty());
            allUsersTable.setItems(sortedData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

