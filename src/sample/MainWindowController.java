package sample;

import database.Database;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    CustomerController customerController;

    @FXML
    ItemController itemController;

    @FXML
    OrderController orderController;

    @FXML
    Tab itemTab;

    @FXML
    Tab orderTab;

    @FXML
    Tab customerTab;


    @Override
    public void initialize(URL location, ResourceBundle resources){

        /*
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("itemTab.fxml"));
        AnchorPane itemPane = null;

        try {
            itemPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        itemTab.setContent(itemPane);
        itemController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader(getClass().getResource("orderTab.fxml"));
        AnchorPane orderPane = null;
        try {
            orderPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        orderTab.setContent(orderPane);
        orderController = fxmlLoader.getController();

         */





  /*
        itemController.itemsChanged.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (itemController.itemsChanged.get()) {
                    orderController.updateItemNames();
                    itemController.itemsChanged.setValue(false);
                }
            }
        });


        customerController.customersChanged.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (customerController.customersChanged.get()) {
                    orderController.updateItemNames();
                    customerController.customersChanged.setValue(false);
                }
            }
        });


         */

    }
}
