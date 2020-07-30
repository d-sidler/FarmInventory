package sample;

import database.Database;
import dialogs.Confirmation;
import dialogs.Warning;
import item.ItemData;
import item.ItemUnit;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import javax.xml.crypto.Data;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ItemController implements Initializable {


    protected BooleanProperty itemsChanged = new SimpleBooleanProperty(false);


    // remembers, which item is currently displayed
    private ItemData displayedItem = null;
    private OperationMode itemMode = OperationMode.DISPLAY;

    // define context menu components
    private ContextMenu contextMenu;
    private MenuItem displayMenu;
    private MenuItem editMenu;
    private MenuItem deleteMenu;

    // store item data
    private ObservableList<ItemData> itemData;

    protected ObservableList<ItemData> getItemData() {
        return itemData;
    }

    // GUI components
    @FXML
    private ImageView item_placeholder_image;

    @FXML
    private TableView<ItemData> itemTable;

    @FXML
    private TableColumn<ItemData, String> item_column_name;

    @FXML
    private TableColumn<ItemData, Double> item_column_price;

    @FXML
    private TableColumn<ItemData, String> item_column_unit;

    @FXML
    private VBox itemBox;

    @FXML
    private TextField itemNameField;

    @FXML
    private TextField itemPriceField;

    @FXML
    private ComboBox<ItemUnit> itemUnitBox;

    @FXML
    private Button itemSaveButton;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        contextMenu = new ContextMenu();
        displayMenu = new MenuItem("Anzeigen");
        editMenu = new MenuItem("Bearbeiten");
        deleteMenu = new MenuItem("Löschen");
        contextMenu.getItems().addAll(displayMenu, editMenu, deleteMenu);

        itemBox.setVisible(false);

        itemUnitBox.setItems( FXCollections.observableArrayList( ItemUnit.values()) );
        loadItemData();


        item_placeholder_image.setImage(new Image("/resources/picture_placeholder.png"));

        // TODO: set context menu upon right click on table row
        itemTable.setRowFactory( tv -> {
            TableRow<ItemData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 ) {
                    if (row.isEmpty()) {
                        displayItem(null);
                    } else {
                        ItemData rowData = row.getItem();
                        displayItem(rowData);
                    }
                }
                if(event.getButton() == MouseButton.SECONDARY) {
                    if (! row.isEmpty()) {

                        ItemData rowData = row.getItem();

                        displayMenu.setOnAction(e->displayItem(rowData));
                        editMenu.setOnAction(e-> {
                            displayItem(rowData);
                            setItemOperationMode(OperationMode.EDIT);
                        });
                        deleteMenu.setOnAction(e-> {
                            deleteItem(rowData);
                        });

                        contextMenu.show(itemTable, event.getScreenX(), event.getScreenY());
                    }
                }
            });
            return row;
        });

        //itemTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadItemData() {
        this.itemData = FXCollections.observableArrayList();

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "SELECT * FROM items";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            while (rs.next()) {
                this.itemData.add(new ItemData(rs.getInt(1), rs.getString(2),
                        rs.getDouble(3), rs.getString(4)));
            }
            dbConnection.close();
        } catch (SQLException e) {
            System.out.println("Error loading Item Data");
        }

        this.item_column_name.setCellValueFactory(new PropertyValueFactory<ItemData, String>("itemName"));

        this.item_column_price.setCellValueFactory(new PropertyValueFactory<ItemData, Double>("price"));
        this.item_column_price.setCellFactory(tc -> new TableCell<ItemData, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f Fr.", price));
                }
            }
        });

        this.item_column_unit.setCellValueFactory(new PropertyValueFactory<ItemData, String>("priceUnitString"));

        this.itemTable.setItems(null);
        this.itemTable.setItems(itemData);

    }

    void setItemOperationMode(OperationMode operationMode) {
        itemMode = operationMode;

        if (operationMode == OperationMode.EDIT || operationMode == OperationMode.INSERT) {
            itemSaveButton.setText("Speichern");
            setItemEditable(true);
        }
        if (operationMode == OperationMode.DISPLAY) {
            itemSaveButton.setText("Ware Bearbeiten");
            setItemEditable(false);
        }
    }

    @FXML
    void addItemButtonPressed(ActionEvent event) {

        itemMode = OperationMode.INSERT;

        itemNameField.setText("");
        itemPriceField.setText("");
        itemUnitBox.cancelEdit();

        itemBox.setVisible(true);
        setItemEditable(true);
    }

    void displayItem(ItemData itemData) {

        displayedItem = itemData;
        setItemOperationMode(OperationMode.DISPLAY);

        if (itemData == null) {
            itemBox.setVisible(false);
        } else {
            itemBox.setVisible(true);

            itemNameField.setText(itemData.getItemName());
            itemPriceField.setText(String.format("%.2f", itemData.getPrice()));
            itemUnitBox.setValue(ItemUnit.kg);
            itemUnitBox.setValue(ItemUnit.valueOf(itemData.getPriceUnit()));

            setItemEditable(false);
        }

    }

    void setItemEditable(boolean editable) {
        itemNameField.setEditable(editable);
        itemPriceField.setEditable(editable);
        itemUnitBox.setDisable(!editable);

        if (editable) {
            itemSaveButton.setText("Speichern");
        } else {
            itemSaveButton.setText("Ware Bearbeiten");
        }
    }

    @FXML
    void editAndSaveItem(ActionEvent event) {

        switch (itemMode) {
            case DISPLAY:
                setItemOperationMode(OperationMode.EDIT);
                break;
            case EDIT:
                editItem(displayedItem);
                break;
            case INSERT:
                insertItem();
                break;
            default:
                System.out.println("Error: Invalid OperationMode set");
        }

    }

    void editItem(ItemData itemData) {

        double insertItemPrice;
        try {
            insertItemPrice = Double.parseDouble(this.itemPriceField.getText());
        }
        catch (NumberFormatException e) {
            Warning.display("\" " + this.itemPriceField.getText() + " \" ist kein gültiger Preis" );
            return;
        }


        try {
            Connection dbConnection = Database.getConnection();

            String sql = "UPDATE items " +
                    "SET itemName = ?, price = ?, priceUnit = ? " +
                    "WHERE id = ?";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1, this.itemNameField.getText());
            st.setDouble(2, insertItemPrice);
            st.setString(3, this.itemUnitBox.getValue().toString());
            st.setInt(4, displayedItem.id().get());

            st.execute();

            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

        loadItemData();
        itemsChanged.setValue(true);

        setItemOperationMode(OperationMode.DISPLAY);
    }

    void insertItem() {

        String insertItemName = this.itemNameField.getText();
        if (itemNameExists(insertItemName)) {
            Warning.display("Eintrag \" " + insertItemName + " \" existiert bereits");
            return;
        }

        double insertItemPrice;
        try {
            insertItemPrice = Double.parseDouble(this.itemPriceField.getText());
        }
        catch (NumberFormatException e) {
            Warning.display("\" " + this.itemPriceField.getText() + " \" ist kein gültiger Preis" );
            return;
        }

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "INSERT INTO items(itemName, price, priceUnit)  VALUES (?,?,?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1, insertItemName);
            st.setDouble(2,insertItemPrice);
            st.setString(3, this.itemUnitBox.getValue().toString());
            st.execute();
            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }


        try {
            Connection dbConnection = Database.getConnection();

            String sql = "SELECT * FROM items WHERE itemName = ?";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1, itemNameField.getText());
            ResultSet rs = st.executeQuery();

            displayedItem = new ItemData(rs.getInt(1), rs.getString(2),
                    rs.getDouble(3), rs.getString(4));

            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }



        loadItemData();
        itemsChanged.setValue(true);


        setItemOperationMode(OperationMode.DISPLAY);

        // TODO: check for double parsing errors
    }

    void deleteItem(ItemData itemData) {
        boolean confirmed = Confirmation.display("Are you sure to delete item \" " + itemData.getItemName() + " \"?");
        if (!confirmed) {
            return;
        }

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "DELETE FROM items WHERE id = ?";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setInt(1, itemData.id().getValue());
            st.execute();
            dbConnection.close();

        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

        loadItemData();
        itemsChanged.setValue(true);
    }

    boolean itemNameExists(String name) {

        boolean exists = false;

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "SELECT id FROM items WHERE LOWER(itemName) LIKE LOWER(?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1,name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                exists = true;
            }
            dbConnection.close();

        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

        return exists;
    }
}
