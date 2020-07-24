package sample;

import com.sun.source.tree.Tree;
import customer.CustomerData;
import dialogs.Warning;
import item.ItemData;
import item.ItemUnit;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import order.OrderData;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.textfield.TextFields;

import database.Database;
import org.w3c.dom.Text;
import java.util.HashMap;

public class OrderController implements Initializable {

    Connection dbConnection;

    private ObservableList<ItemCounter> itemCounters;

    private ObservableList<ItemData> itemData;
    private HashMap<Integer, ItemData> itemDataMap = new HashMap<>();

    private ObservableList<CustomerData> customerData;
    private HashMap<Integer, CustomerData> customerDataMap = new HashMap<>();

    private ObservableList<OrderData> orderData;
    private List<Integer> orderIDs;

    @FXML
    private ComboBox<ItemData> warenDropdown;

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
    void addOrder(ActionEvent event) {

        LocalDate value = this.datePicker.getValue();

        Date orderDate = Date.valueOf(LocalDate.now());
        Date harvestDate = Date.valueOf(datePicker.getValue());

        int orderID = getMaxOrderID() + 1;

        for (ItemCounter ic : itemCounters) {
            ItemData item = ic.getItem();
            System.out.println("ITEM ADDED");
            System.out.println(item);
            Double amount = ic.getAmount();

           insertOrder(
                    orderID,
                    customerDropdown.getValue().id().get(),
                    ic.getItem().id().get(),
                    amount,
                    orderDate,
                    harvestDate
            );
        }

        loadOrderData();
    }

    /*
    @FXML
    void addOrderEntry(ActionEvent event) {
        LocalDate value = this.datePicker.getValue();
        System.out.println(value);
        System.out.println(LocalDate.now());

        Date orderDate = Date.valueOf(LocalDate.now());
        Date harvestDate = Date.valueOf(datePicker.getValue());
        insertOrder(1,
                customerDropdown.getValue().id().get(),
                warenDropdown.getValue().id().get(),
                Double.parseDouble(amountField.getText()),
                orderDate,
                harvestDate
        );

        loadOrderData();
    }

     */

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
            loadOrderData();
        }

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
            String sql = "SELECT MAX(orderID) FROM orders";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            if (rs.next()) {
                maxID = rs.getInt(1);
            }
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

        orderBox.setVisible(false);

        try {
            dbConnection = Database.getConnection();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        updateItemNames();

        warenDropdown.setItems(itemData);
        //TextFields.bindAutoCompletion(warenDropdown.getEditor(), itemData);
        //TextFields.bindAutoCompletion(warenDropdown, itemData);
        warenDropdown.valueProperty().addListener(new ChangeListener<ItemData>() {
            @Override
            public void changed(ObservableValue<? extends ItemData> observable, ItemData oldValue, ItemData newValue) {
                String unit = newValue.getPriceUnit().toString();
                unitField.setText(unit);
            }
        });

        updateCustomerNames();

        customerDropdown.setItems(customerData);


        loadOrderData();

    }

    private void updateItemNames() {
        this.itemData = FXCollections.observableArrayList();

        try {
            String sql = "SELECT * FROM items";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            while (rs.next()) {
                this.itemData.add(new ItemData(rs.getInt(1), rs.getString(2),
                        rs.getDouble(3), rs.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (ItemData d : itemData) {
            itemDataMap.put(d.id().get(), d);
        }
    }

    private void updateCustomerNames() {
        this.customerData = FXCollections.observableArrayList();

        try {
            String sql = "SELECT * FROM customers";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            while (rs.next()) {
                this.customerData.add(new CustomerData(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        for (CustomerData d : customerData) {
            customerDataMap.put(d.id().get(), d);
        }
    }

    private void insertOrder(int orderID, int customerID, int itemID, double itemAmount, Date orderDate, Date harvestDate) {

        try {
            String sql = "INSERT into orders(orderID, customerID, itemID, itemAmount, orderDate, harvestDAte) VALUES (?,?,?,?,?,?)";
            PreparedStatement st = dbConnection.prepareStatement(sql);
            st.setInt(1, orderID);
            st.setInt(2, customerID);
            st.setInt(3, itemID);
            st.setDouble(4, itemAmount);
            st.setDate(5, orderDate);
            st.setDate(6, harvestDate);

            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }



/*
    private void loadOrderData() {
        this.orderData = FXCollections.observableArrayList();

        try {
            String sql = "SELECT * FROM orders";
            ResultSet rs = dbConnection.createStatement().executeQuery(sql);
            while (rs.next()) {
                //Integer orderItemID, Integer orderID, Integer customerID, Integer itemID, Date orderDate, Date harvestDate
                this.orderData.add(
                        new OrderData(
                                rs.getInt(1),
                                rs.getInt(2),
                                rs.getInt(3),
                                rs.getInt(4),
                                rs.getDouble(5),
                                rs.getDate(6),
                                rs.getDate(7)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TreeItem<OrderData> rootOrder = new TreeItem<>(
                new OrderData(1,1,1,0,0., Date.valueOf(LocalDate.now()),Date.valueOf(LocalDate.now()))
        );

        for (OrderData o : orderData) {
            rootOrder.getChildren().add(new TreeItem<>(o));
        }

 */

        private void loadOrderData() {
            this.orderData = FXCollections.observableArrayList();

            try {
                String sql = "SELECT * FROM orders";
                ResultSet rs = dbConnection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    //Integer orderItemID, Integer orderID, Integer customerID, Integer itemID, Date orderDate, Date harvestDate
                    this.orderData.add(
                            new OrderData(
                                    rs.getInt(1),
                                    rs.getInt(2),
                                    rs.getInt(3),
                                    rs.getInt(4),
                                    rs.getDouble(5),
                                    rs.getDate(6),
                                    rs.getDate(7)
                            )
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                String sql = "SELECT DISTINCT orderID FROM orders";
                ResultSet rs = dbConnection.createStatement().executeQuery(sql);
                this.orderIDs = new ArrayList<>();

                while (rs.next()) {
                    this.orderIDs.add(rs.getInt(1));
                }
                System.out.println(orderIDs);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

            TreeItem<OrderData> rootOrder = new TreeItem<>(
                    new OrderData(1,1,1,0,0., Date.valueOf(LocalDate.now()),Date.valueOf(LocalDate.now()))
            );

            //List<TreeItem<OrderData>> separateOrders = new ArrayList<>();
            HashMap<Integer, TreeItem<OrderData>> separateOrders = new HashMap<>();

            for (int i : orderIDs) {
                Date dateTmp =  Date.valueOf(LocalDate.now());
                OrderData tmp = new OrderData(1, 1, 1, 0, 0., dateTmp, dateTmp);
                TreeItem<OrderData> tmpItem = new TreeItem<>(tmp);
                separateOrders.put(i, tmpItem);
                rootOrder.getChildren().add(tmpItem);
            }


            for (OrderData o : orderData) {
                int newID = o.getOrderID().get();

                separateOrders.get(newID).getChildren().add(new TreeItem<>(o));
            }





        orderTableCustomer.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<OrderData, String> param) {
                Integer customerID = param.getValue().getValue().getCustomerID().get();
                return new SimpleStringProperty(customerDataMap.get(customerID).toString());

                //return param.getValue().getValue().getCustomerID();
            }
        });


        orderTableItem.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<OrderData, String> param) {
                StringProperty itemName;

                Integer itemID = param.getValue().getValue().getItemID().get();
                if (itemID == 0) {
                    itemName = new SimpleStringProperty("...");
                } else {
                    itemName = itemDataMap.get(itemID).itemName();
                }
                return itemName;
            }
        });

        orderTableAmount.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<OrderData, String> param) {
                String amountString = "";

                Integer itemID = param.getValue().getValue().getItemID().get();

                if (itemID==0) {
                    amountString = ("...");
                } else {
                    String unit = itemDataMap.get(itemID).getPriceUnit().toString();
                    Double amount =  param.getValue().getValue().getItemAmount().get();
                    amountString = String.format("%.2f %s", amount, unit);
                }

                return new SimpleStringProperty(amountString);

            }
        });

        orderTableOrderDate.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<OrderData, Date>, ObservableValue<Date>>() {
            @Override
            public ObservableValue<Date> call(TreeTableColumn.CellDataFeatures<OrderData, Date> param) {
                return param.getValue().getValue().getOrderDate();
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
