package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.scene.control.TableView;

public class PlayListController extends Application {
    private TableView tableview;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        tableview = new TableView();
    }
}
