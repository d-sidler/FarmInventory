package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ImageView user_placeholder_image;

    @FXML
    private ImageView item_placeholder_image;

    @FXML
    private Button save_item_button;

    @FXML
    void push_button(ActionEvent event) {
        System.out.println("Push Me");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user_placeholder_image.setImage(new Image("/resources/picture_placeholder.png"));
        item_placeholder_image.setImage(new Image("/resources/picture_placeholder.png"));

        Image image = new Image(getClass().getResourceAsStream("/resources/rightarrow.png"));

        //save_item_button.setGraphic(new ImageView(image));
        //save_item_button.setContentDisplay(ContentDisplay.RIGHT);
        //save_item_button.setTextAlignment(TextAlignment.LEFT);
        //save_item_button.setGraphicTextGap(20);

    }
}
