package dialogs;

import javafx.scene.control.Alert;

public class Warning {

    public static void display(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warnung");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }
}
