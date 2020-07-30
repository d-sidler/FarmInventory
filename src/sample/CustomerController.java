package sample;

import database.Database;
import dialogs.Confirmation;
import dialogs.Warning;
import customer.CustomerData;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

public class CustomerController implements Initializable {

    protected BooleanProperty customersChanged = new SimpleBooleanProperty(false);

    // remembers, which item is currently displayed
    private CustomerData displayedCustomer = null;
    private OperationMode customerMode = OperationMode.DISPLAY;

    // define context menu components
    private ContextMenu contextMenu;
    private MenuItem displayMenu;
    private MenuItem editMenu;
    private MenuItem deleteMenu;

    // store customer data
    private ObservableList<CustomerData> customerData;

    protected ObservableList<CustomerData> getCustomerData() {
        return customerData;
    }

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
    private TableView<CustomerData> customerTable;

    @FXML
    private TableColumn<CustomerData, String> customer_column_fname;

    @FXML
    private TableColumn<CustomerData, String> customer_column_lname;

    @FXML
    private TableColumn<CustomerData, String> customer_column_email;

    @FXML
    private ImageView peopleImage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        contextMenu = new ContextMenu();
        displayMenu = new MenuItem("Anzeigen");
        editMenu = new MenuItem("Bearbeiten");
        deleteMenu = new MenuItem("Löschen");
        contextMenu.getItems().addAll(displayMenu, editMenu, deleteMenu);

        customerBox.setVisible(false);

        loadCustomerData();

        user_placeholder_image.setImage(new Image("/resources/picture_placeholder.png"));
        peopleImage.setImage(new Image("/resources/people_banner.png"));

        // TODO: set context menu upon right click on table row
        customerTable.setRowFactory( tv -> {
            TableRow<CustomerData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 ) {
                    if (row.isEmpty()) {
                        displayCustomer(null);
                    } else {
                        CustomerData rowData = row.getItem();
                        displayCustomer(rowData);
                    }
                }
                if(event.getButton() == MouseButton.SECONDARY) {
                    if (! row.isEmpty()) {

                        CustomerData rowData = row.getItem();

                        displayMenu.setOnAction(e->displayCustomer(rowData));
                        editMenu.setOnAction(e-> {
                            displayCustomer(rowData);
                            setCustomerOperationMode(OperationMode.EDIT);
                        });
                        deleteMenu.setOnAction(e-> {
                            deleteCustomer(rowData);
                        });

                        contextMenu.show(customerTable, event.getScreenX(), event.getScreenY());
                    }
                }
            });
            return row;
        });



    }

    private void loadCustomerData() {

        try {
            Connection dbConnection = Database.getConnection();
            this.customerData = FXCollections.observableArrayList();

            String sql = "SELECT * FROM customers";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            while (rs.next()) {
                this.customerData.add(new CustomerData(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)));
            }


            this.customer_column_fname.setCellValueFactory(new PropertyValueFactory<CustomerData, String>("firstName"));
            this.customer_column_lname.setCellValueFactory(new PropertyValueFactory<CustomerData, String>("lastName"));
            this.customer_column_email.setCellValueFactory(new PropertyValueFactory<CustomerData, String>("email"));

            this.customerTable.setItems(null);
            this.customerTable.setItems(customerData);
            dbConnection.close();

        }
        catch (SQLException e) {
            System.out.println("Error Loading Customer Database");
        }
    }


    void setCustomerOperationMode(OperationMode operationMode) {
        customerMode = operationMode;

        if (operationMode == OperationMode.EDIT || operationMode == OperationMode.INSERT) {
            customerSaveButton.setText("Speichern");
            setCustomerEditable(true);
        }
        if (operationMode == OperationMode.DISPLAY) {
            customerSaveButton.setText("Kunde Bearbeiten");
            setCustomerEditable(false);
        }
    }


    @FXML
    void addCustomerButtonPressed(ActionEvent event) {

        customerMode = OperationMode.INSERT;

        customerFnameField.setText("");
        customerLnameField.setText("");
        customerEmailField.setText("");

        customerBox.setVisible(true);
        setCustomerEditable(true);
    }


    void displayCustomer(CustomerData customerData) {

        displayedCustomer = customerData;
        setCustomerOperationMode(OperationMode.DISPLAY);

        if (customerData == null) {
            customerBox.setVisible(false);
        } else {
            customerBox.setVisible(true);

            customerFnameField.setText(customerData.getFirstName());
            customerLnameField.setText(customerData.getLastName());
            customerEmailField.setText(customerData.getEmail());

            setCustomerEditable(false);
        }

    }


    void setCustomerEditable(boolean editable) {
        customerFnameField.setEditable(editable);
        customerLnameField.setEditable(editable);
        customerEmailField.setEditable(editable);

        if (editable) {
            customerSaveButton.setText("Speichern");
        } else {
            customerSaveButton.setText("Kunde Bearbeiten");
        }
    }


    @FXML
    void editAndSaveCustomer(ActionEvent event) {

        switch (customerMode) {
            case DISPLAY:
                setCustomerOperationMode(OperationMode.EDIT);
                break;
            case EDIT:
                editCustomer(displayedCustomer);
                break;
            case INSERT:
                insertCustomer();
                break;
            default:
                System.out.println("Error: Invalid OperationMode set");
        }

    }


    void editCustomer(CustomerData customerData) {

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "UPDATE customers " +
                    "SET firstName = ?, lastName = ?, email = ? " +
                    "WHERE id = ?";

            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1, this.customerFnameField.getText());
            st.setString(2, this.customerLnameField.getText());
            st.setString(3, this.customerEmailField.getText());
            st.setInt(4, displayedCustomer.id().get());

            st.execute();
            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }


        loadCustomerData();
        customersChanged.setValue(true);

        setCustomerOperationMode(OperationMode.DISPLAY);
    }

    void insertCustomer() {

        String insertCustomerFname = this.customerFnameField.getText();
        String insertCustomerLname = this.customerLnameField.getText();
        String insertCustomerEmail = this.customerEmailField.getText();

        if (customerNameExists(insertCustomerFname, insertCustomerLname)) {
            Warning.display("Kunde \" " + insertCustomerFname + " " + insertCustomerLname + " \" existiert bereits");
            return;
        }

        try {
            Connection dbConnection = Database.getConnection();
            String sql = "INSERT INTO customers(firstName, lastName, email)  VALUES (?,?,?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1, insertCustomerFname);
            st.setString(2, insertCustomerLname);
            st.setString(3, insertCustomerEmail);

            st.execute();
            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }


        try {
            Connection dbConnection = Database.getConnection();
            String sql = "SELECT * FROM customers WHERE LOWER(firstName) = LOWER(?) AND LOWER(lastName) = LOWER(?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1, insertCustomerFname);
            st.setString(2, insertCustomerLname);
            ResultSet rs = st.executeQuery();

            displayedCustomer = new CustomerData(rs.getInt(1), rs.getString(2),
                    rs.getString(3), rs.getString(4));

            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

        loadCustomerData();
        customersChanged.setValue(true);

        setCustomerOperationMode(OperationMode.DISPLAY);

        // TODO: check for double parsing errors

    }



    void deleteCustomer(CustomerData cData) {
        boolean confirmed = Confirmation.display("Are you sure to delete item \" " + cData.getFirstName() + " " + cData.getLastName() + " \"?");
        if (!confirmed) {
            return;
        }

        try {
            Connection dbConnection = Database.getConnection();
            String sql = "DELETE FROM customers WHERE id = ?";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setInt(1, cData.id().getValue());
            st.execute();
            dbConnection.close();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

        loadCustomerData();
        customersChanged.setValue(true);
    }


    boolean customerNameExists(String fname, String lname) {

        boolean exists = false;

        try {
            Connection dbConnection = Database.getConnection();
            String sql = "SELECT id FROM customers WHERE LOWER(firstName) LIKE LOWER(?) and lower(lastName) LIKE LOWER(?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setString(1,fname);
            st.setString(2,lname);
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
