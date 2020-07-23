package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML
    private ImageView user_placeholder_image;

    @FXML
    private VBox customerBox;

    @FXML
    private TextField customerFnameField;

    @FXML
    private TextField customerLnameField;

    @FXML
    private TextField customerEmailField;

    @FXML
    private Button customerSaveButton;

    @FXML
    void editAndSaveCustomer(ActionEvent event) {

    }

    @FXML
    void addCustomerButtonPressed(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user_placeholder_image.setImage(new Image("/resources/picture_placeholder.png"));
    }
}
