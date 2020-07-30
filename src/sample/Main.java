package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import database.Database;

public class Main extends Application {

    @FXML
    AnchorPane orderTab;


    @Override
    public void start(Stage primaryStage) throws Exception{

        Database.initializeTables();

        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));


        primaryStage.setTitle("Farm Inventory");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.getIcons().add(new Image("/resources/icon.png"));
        primaryStage.setResizable(false);

        primaryStage.show();


    }


    public static void main(String[] args) {
        launch(args);
    }
}
