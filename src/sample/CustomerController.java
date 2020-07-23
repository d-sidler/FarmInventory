package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML
    private ImageView user_placeholder_image;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user_placeholder_image.setImage(new Image("/resources/picture_placeholder.png"));
    }
}
