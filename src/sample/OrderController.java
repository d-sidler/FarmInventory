package sample;

import customer.CustomerData;
import dialogs.Warning;
import impl.org.controlsfx.tools.PrefixSelectionCustomizer;
import item.ItemData;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.print.PrinterJob;

import java.net.URL;
import java.sql.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import order.OrderData;


import database.Database;
import org.controlsfx.control.PrefixSelectionChoiceBox;

import java.util.HashMap;

public class OrderController implements Initializable {


    private ObservableList<ItemCounter> itemCounters;

    private ObservableList<ItemData> itemData;
    private HashMap<Integer, ItemData> itemDataMap = new HashMap<>();

    private ObservableList<CustomerData> customerData;
    private HashMap<Integer, CustomerData> customerDataMap = new HashMap<>();

    private ObservableList<OrderData> orderData;
    private HashMap<Integer, TreeItem<OrderData>> orderDataMap = new HashMap<>();
    private List<Integer> orderIDs;

    private TreeItem<OrderData> rootOrder;

    @FXML
    private ComboBox<ItemData> warenDropdown;
    //private AutoCompleteComboBoxListener<ItemData> aWarenDropdown = new AutoCompleteComboBoxListener<>(warenDropdown);

    @FXML
    private ComboBox<CustomerData> customerDropdown;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TreeTableView<OrderData> orderTable;

    @FXML
    private TreeTableColumn<OrderData, String> orderTableCustomer;

    @FXML
    private TreeTableColumn<OrderData, String> orderTableItem;

    @FXML
    private TreeTableColumn<OrderData, String> orderTableAmount;

    @FXML
    private TreeTableColumn<OrderData, Date> orderTableOrderDate;

    @FXML
    private TreeTableColumn<OrderData, Date> orderTableDeliveryDate;

    @FXML
    private TextField amountField;

    @FXML
    private TextField unitField;

    @FXML
    private VBox orderBox;

    @FXML
    private TableView<ItemCounter> addTable;

    @FXML
    private TableColumn<ItemCounter, String> addTableItem;

    @FXML
    private TableColumn<ItemCounter, String> addTableAmount;

    @FXML
    private TitledPane orderAcc;

    @FXML
    private CheckBox show_finished_checkbox;

    @FXML
    private ComboBox<CustomerData> filter_customer;


    @FXML
    void generate_harvest_plan(ActionEvent event) {

    }

    @FXML
    void apply_customer_filter(ActionEvent event) {

    }


    boolean checkValidOrder() {
        boolean validOrder = true;

        if (datePicker.getValue() == null) {
            //datePicker.getEditor().setStyle("-fx-border-color: red");
            datePicker.setStyle("-fx-border-color: red");
            validOrder = false;
        } else {
            datePicker.setStyle("-fx-border-color: transparent");
        }

        if (customerDropdown.getValue() == null) {
            customerDropdown.setStyle("-fx-border-color: red");
            validOrder = false;
        } else {
            customerDropdown.setStyle("-fx-border-color: transparent");
        }

        if (itemCounters.size() == 0) {
            addTable.setStyle("-fx-border-color: red");
            validOrder = false;
        } else {
            addTable.setStyle("-fx-border-color: transparent");
        }

        return validOrder;

    }

    @FXML
    void addOrder(ActionEvent event) {


        checkValidOrder();

        LocalDate value = this.datePicker.getValue();

        Date orderDate = Date.valueOf(LocalDate.now());

        Date harvestDate = null;
        try {
            harvestDate = Date.valueOf(datePicker.getValue());
        }
        catch (NullPointerException e) {
            Warning.display("Das Lieferdatum ist noch nicht festgelegt");
            return;
        }

        int customerID = 0;
        try {
            customerID = customerDropdown.getValue().id().get();
        }
        catch (NullPointerException e) {
            Warning.display("Der Kunde ist noch nicht definiert");
            return;
        }

        if (itemCounters.size()==0) {
            Warning.display("Nch keine Waren hinzugefügt");
            return;
        }

        int orderID = insertOrder(
                customerID,
                orderDate,
                harvestDate
        );

        // generate order to insert into table
        OrderData insert = new OrderData(
                0,
                orderID,   //orderID
                customerID, //customerID
                0,
                0.,
                orderDate,    // orderDate
                harvestDate,    // harvestDate
                false  // finished
        );

        orderData.add(insert);
        orderDataMap.put(orderID, new TreeItem<OrderData>(insert));



        for (ItemCounter ic : itemCounters) {
            ItemData item = ic.getItem();
            Double amount = ic.getAmount();

            int orderItemID = insertOrderItem(  // toDo: insertOrderItem should return the generated object already
                    orderID,
                    item.id().get(),
                    amount
            );

            OrderData insertItem = new OrderData(
                    orderItemID, // orderItemID
                    orderID, // orderID
                    0,
                    item.id().get(), // itemID
                    amount, // itemAmount
                    null,
                    null,
                    false
            );

            orderDataMap.get(orderID).getChildren().add(new TreeItem<>(insertItem));

        }


        rootOrder.getChildren().add(orderDataMap.get(orderID));

    }

    @FXML
    void addOrderEntry(ActionEvent event) {
        ItemData item = warenDropdown.getValue();
        System.out.println("ITEM");
        System.out.println(item);
        Double amount = Double.parseDouble(amountField.getText());

        if (itemExists(item)) {
            Warning.display("Ware \"" + item.getItemName() + "\" wurde bereits hinzugefügt" );
        } else {
            itemCounters.add(new ItemCounter(item, amount));
            //loadOrderData();
        }

    }


    @FXML
    void hide_all(ActionEvent event) {
        for (TreeItem<OrderData> o : orderDataMap.values()) {
            o.setExpanded(false);
        }
    }

    @FXML
    void show_all(ActionEvent event) {
        for (TreeItem<OrderData> o : orderDataMap.values()) {
            o.setExpanded(true);
        }
    }

    @FXML
    void show_finished(ActionEvent event) {

        rootOrder = new TreeItem<>();

        for (TreeItem<OrderData> o : orderDataMap.values()) {

            LocalDate orderDate = o.getValue().getHarvestDate().get().toLocalDate();
            LocalDate now = LocalDate.now().minusDays(1);

            if (orderDate.isAfter(now) || show_finished_checkbox.isSelected()) {
                rootOrder.getChildren().add(o);
            }

        }

        orderTable.setRoot(rootOrder);
        orderTableDeliveryDate.setSortType(TreeTableColumn.SortType.ASCENDING);
        //orderTableCustomer.setSortType(TreeTableColumn.SortType.ASCENDING);
        orderTable.getSortOrder().add(orderTableDeliveryDate);

    }


    @FXML
    void newOrder(ActionEvent event) {

        orderBox.setVisible(true);


        itemCounters =FXCollections.observableArrayList();
        addTable.setItems(null);

        updateAddTable();
    }

    private int getMaxOrderID() {

        int maxID = 0;

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "SELECT MAX(orderID) FROM orders";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            if (rs.next()) {
                maxID = rs.getInt(1);
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maxID;
    }

    private void updateAddTable() {
        this.addTableItem.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ItemCounter, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ItemCounter, String> param) {
                return param.getValue().getItem().itemName();
            }
        });

        this.addTableAmount.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ItemCounter, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ItemCounter, String> param) {
                String amount = param.getValue().getAmount().toString() + " " + param.getValue().getItem().getPriceUnit();
                return new SimpleStringProperty(amount);
            }
        });

        this.addTable.setItems(null);
        this.addTable.setItems(this.itemCounters);

    }

    private boolean itemExists(ItemData dataCheck) {
        boolean exists = false;
        for (ItemCounter ic : itemCounters) {
            ItemData data = ic.getItem();
            if (data.id().get() == dataCheck.id().get()) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        orderAcc.setExpanded(true);
        orderBox.setVisible(false);

        updateItemNames();

        warenDropdown.setItems(itemData);
        new ComboBoxAutoComplete<ItemData>(warenDropdown);


        warenDropdown.valueProperty().addListener(new ChangeListener<ItemData>() {
            @Override
            public void changed(ObservableValue<? extends ItemData> observable, ItemData oldValue, ItemData newValue) {
                if (newValue != null) {
                    unitField.setText("/ " + newValue.getPriceUnit());
                }
            }
        });


        updateCustomerNames();

        customerDropdown.setItems(customerData);
        new ComboBoxAutoComplete<CustomerData>(customerDropdown);



        filter_customer.setItems(customerData);
        new ComboBoxAutoComplete<CustomerData>(filter_customer);


        loadOrderData();


    }

    protected void updateItemNames() {
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
            e.printStackTrace();
        }

        for (ItemData d : itemData) {
            itemDataMap.put(d.id().get(), d);
        }
    }

    protected void updateCustomerNames() {
        this.customerData = FXCollections.observableArrayList();

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "SELECT * FROM customers";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            while (rs.next()) {
                this.customerData.add(new CustomerData(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)));
            }
            dbConnection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        for (CustomerData d : customerData) {
            customerDataMap.put(d.id().get(), d);
        }
    }

    private int insertOrder(int customerID, Date orderDate, Date harvestDate) {

        int lastIndex = 0;

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "INSERT INTO orders(customerID, orderDate, harvestDate) VALUES (?,?,?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setInt(1,customerID);
            st.setDate(2,orderDate);
            st.setDate(3,harvestDate);
            st.execute();
            lastIndex = st.getGeneratedKeys().getInt(1);
            dbConnection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return lastIndex;
    }

    private int insertOrderItem(int orderID, int itemID, double itemAmount) {

        int lastIndex = 0;

        try {
            Connection dbConnection = Database.getConnection();

            String sql = "INSERT INTO orderItems(orderID, itemID, itemAmount) VALUES (?,?,?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setInt(1,orderID);
            st.setInt(2,itemID);
            st.setDouble(3, itemAmount);
            st.execute();
            lastIndex = st.getGeneratedKeys().getInt(1);
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lastIndex;
    }

    private void loadOrderData() {
        this.orderData = FXCollections.observableArrayList();
        this.orderDataMap = new HashMap<>();

        // define root
        rootOrder = new TreeItem<>(
                new OrderData(1,1,1,0,0., Date.valueOf(LocalDate.now()),Date.valueOf(LocalDate.now()), false)
        );


        // generate order layer
        try {
            Connection dbConnection = Database.getConnection();

            String sql = "SELECT * FROM orders";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);

            while (rs.next()) {
                //Integer orderItemID, Integer orderID, Integer customerID, Integer itemID, Date orderDate, Date harvestDate

                OrderData insert = new OrderData(
                                0,
                                rs.getInt(1),     //orderID
                                rs.getInt(2),     //customerID
                                0,
                                0.,
                                rs.getDate(3),    // orderDate
                                rs.getDate(4),    // harvestDate
                                rs.getBoolean(5)  // finished
                );

                orderData.add(insert);
                TreeItem<OrderData> orderCollection = new TreeItem<>(insert);
                orderDataMap.put(insert.getOrderID().get(), orderCollection);

            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }





        try {
            Connection dbConnection = Database.getConnection();

            String sql    = "SELECT * FROM orderItems";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            Date today = Date.valueOf(LocalDate.now());

            while (rs.next()) {
                //Integer orderItemID, Integer orderID, Integer customerID, Integer itemID, Double amount, Date orderDate, Date harvestDate

                OrderData insert = new OrderData(
                        rs.getInt(1), // orderItemID
                        rs.getInt(2), // orderID
                        0,
                        rs.getInt(3), // itemID
                        rs.getDouble(4), // itemAmount
                        null,
                        null,
                        false
                );

                orderData.add(insert);
                TreeItem<OrderData> order = new TreeItem<>(insert);
                int orderID = rs.getInt(2);

                orderDataMap.get(orderID).getChildren().add(order);
                //rootOrder.getChildren().add(order);
            }
            dbConnection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (TreeItem<OrderData> o : orderDataMap.values()) {
            /*
            if (o.getValue().getHarvestDate().get().compareTo(Date.valueOf(LocalDate.now())) >= 0 ) {
                rootOrder.getChildren().add(o);
            }

             */

            LocalDate orderDate = o.getValue().getHarvestDate().get().toLocalDate();
            LocalDate now = LocalDate.now().minusDays(1);

            if (orderDate.isAfter(now)) {
                rootOrder.getChildren().add(o);
            }




        }






        orderTableCustomer.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<OrderData, String> param) {
                SimpleStringProperty customerString = null;


                Integer customerID = param.getValue().getValue().getCustomerID().get();
                if (customerID != 0) {
                    customerString = new SimpleStringProperty(customerDataMap.get(customerID).toString());

                }

                return customerString;
                //return param.getValue().getValue().getCustomerID();
            }
        });



        orderTableItem.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<OrderData, String> param) {
                StringProperty itemName=null;

                Integer itemID = param.getValue().getValue().getItemID().get();
                if (itemID != 0) {
                    itemName = itemDataMap.get(itemID).itemName();
                }

                return itemName;
            }
        });


        orderTableAmount.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<OrderData, String> param) {
                StringProperty amountString = null;

                Integer itemID = param.getValue().getValue().getItemID().get();

                if (!(itemID==0)) {
                    String unit = itemDataMap.get(itemID).getPriceUnit().toString();
                    Double amount =  param.getValue().getValue().getItemAmount().get();
                    amountString = new SimpleStringProperty(String.format("%.2f %s", amount, unit));
                }

                return amountString;

            }
        });



        orderTableOrderDate.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(TreeTableColumn.CellDataFeatures<OrderData, Date> param) {
                //Date tmp = param.getValue().getValue().getOrderDate().get();
                //SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");


                return param.getValue().getValue().getOrderDate();
            }
        });


        orderTableOrderDate.setCellFactory(column -> {
            TreeTableCell<OrderData, Date> cell = new TreeTableCell<>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        if (item == null) {
                            setText(null);
                        } else {
                            setText(format.format(item));
                            //setStyle("-fx-font-weight: bold");
                        }
                    }
                }
            };
            return cell;
        });

        orderTableDeliveryDate.setCellFactory(column -> {
            TreeTableCell<OrderData, Date> cell = new TreeTableCell<>() {
                private SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy");

                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        if (item == null) {
                            setText(null);
                        } else {
                            setText(format.format(item));
                            //setStyle("-fx-font-weight: bold");
                        }
                    }
                }
            };
            return cell;
        });


        orderTable.setRowFactory(row -> new TreeTableRow<OrderData>() {
                    @Override
                    public void updateItem(OrderData data, boolean empty) {
                        super.updateItem(data, empty);

                        if (!empty && data != null) {
                            if (data.getItemID().get() == 0) {
                                if (data.getHarvestDate().get().after(Date.valueOf(LocalDate.now().minusDays(1)))) {
                                    setStyle("-fx-background: #dfecf2; -fx-font-weight: bold");
                                } else {
                                    setStyle("-fx-background: lightgrey; -fx-font-weight: bold");
                                }


                            } else {
                                setStyle("-fx-background: white; -fx-font-weight: normal;");
                            }

                        }
                        if (empty) {
                            setStyle("-fx-background: white; ");
                        }
                    }
        });



        orderTableDeliveryDate.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(TreeTableColumn.CellDataFeatures<OrderData, Date> param) {
                return param.getValue().getValue().getHarvestDate();
            }
        });


        orderTable.setRoot(rootOrder);
        orderTable.setShowRoot(false);

        //expandTreeView(rootOrder);
        orderTableDeliveryDate.setSortType(TreeTableColumn.SortType.DESCENDING);
        //orderTableCustomer.setSortType(TreeTableColumn.SortType.ASCENDING);
        orderTable.getSortOrder().add(orderTableDeliveryDate);

    }

    private void expandTreeView(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandTreeView(child);
            }
        }
    }

    class ItemCounter {
        private ItemData item;
        private Double amount;
        ItemCounter(ItemData item, Double amount) {
            this.item = item;
            this.amount = amount;
        }

        public ItemData getItem() {
            return item;
        }

        public Double getAmount() {
            return amount;
        }
    }


}
