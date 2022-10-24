package sample;

import javafx.beans.Observable;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;

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
   /* @FXML
    private TableColumn<ResultSet,?> followColumn;*/

    ObservableList<User> followersList = FXCollections.observableArrayList();

    public void switchToMainPageScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void searchMyFollowers(ActionEvent event) {

        try {
            String query = "SELECT username, email FROM \"User\" WHERE email LIKE '%" +
                    searchField.getText() + "%'";
            ResultSet rs = PostgresSSH.executeSelect(query);

            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                User follower = new User(username, email);
                followersList.addAll(follower);
            }

            usernameColumn.setCellValueFactory(new PropertyValueFactory<User, String>(""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        /*SELECT username, email FROM "User"
        WHERE username IN (SELECT "userFollowing" from "Follows"
                WHERE "userFollowed" = 'hhutcheonsjl');*/

        String viewFollowersQuery = "SELECT username, email FROM \"User\" WHERE username IN" +
                "(SELECT \"userFollowing\" FROM \"Follows\" WHERE \"userFollowed\" = '" +
                Model.self.getUsername() + "')";

        try {
            ResultSet rs = PostgresSSH.executeSelect(viewFollowersQuery);
            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                followersList.add(new User(username, email));
            }

            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

            followersTable.setItems(followersList);

            // filter the list
            FilteredList<User> filteredData = new FilteredList<>(followersList, b -> true);

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
            sortedData.comparatorProperty().bind(followersTable.comparatorProperty());
            followersTable.setItems(sortedData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
