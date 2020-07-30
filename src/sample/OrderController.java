package sample;

import customer.CustomerData;
import dialogs.Warning;
import impl.org.controlsfx.tools.PrefixSelectionCustomizer;
import item.ItemCounter;
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

import java.io.File;
import java.net.URL;
import java.sql.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;
import order.*;

import database.Database;
import org.controlsfx.control.PrefixSelectionChoiceBox;
import pdf.PDFTableWriter;

import javax.swing.*;
import java.util.HashMap;

public class OrderController implements Initializable {


    private ObservableList<ItemCounter> itemCounters;

    private ObservableList<ItemData> itemData;
    private HashMap<Integer, ItemData> itemDataMap = new HashMap<>();

    private ObservableList<CustomerData> customerData;
    private HashMap<Integer, CustomerData> customerDataMap = new HashMap<>();

    private ObservableList<OrderData> orderData;
    private HashMap<Integer, TreeItem<OrderData>> orderDataMap = new HashMap<>();

    private TreeItem<OrderData> rootOrder;

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
    private TitledPane orderAcc;

    @FXML
    private CheckBox show_finished_checkbox;

    @FXML
    private ComboBox<CustomerData> filter_customer;

    @FXML
    private CheckBox filter_customer_checkbox;

    @FXML
    private CheckBox filter_date_checkbox;

    @FXML
    private DatePicker filter_start_date;

    @FXML
    private DatePicker filter_end_date;

    @FXML
    private VBox date_filter_box;

    @FXML
    void selectDateFilter(ActionEvent event) {
        if (filter_date_checkbox.isSelected()) {
            date_filter_box.setVisible(true);
        } else {
            date_filter_box.setVisible(false);
        }
    }

    @FXML
    void selectCustomerFilter(ActionEvent event) {
        if (filter_customer_checkbox.isSelected()) {
            filter_customer.setVisible(true);
        } else {
            filter_customer.setVisible(false);
        }
    }

    @FXML
    void generate_harvest_plan(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("harvestPlan");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", ".pdf"));
        File selectedFile = fileChooser.showSaveDialog(this.orderTable.getScene().getWindow());


        ArrayList<ItemCounter> harvestPlan = harvestPlanFromTable();

        PDFTableWriter writer = new PDFTableWriter(selectedFile.getAbsolutePath());
        writer.setTitle("Zusammenfassung der Bestellungen");
        writer.setTableData(harvestPlan);
        writer.writeContent();

        //writer.addTable(harvestPlan);

        /*
        for (ItemCounter i : harvestPlan) {
            System.out.println(i.getItem() + " " + i.getAmount() + " " + i.getItem().getPriceUnit() );
        }

         */
    }

    private ArrayList<ItemCounter> harvestPlanFromTable() {
        ArrayList<ItemCounter> harvestPlan = new ArrayList<>();
        HashMap<Integer, Double> counter = new HashMap<>();


        for (TreeItem<OrderData> order : rootOrder.getChildren())  {
            for (TreeItem<OrderData> orderItem : order.getChildren()) {
                int itemID = orderItem.getValue().getItemID().get();
                double itemAmount = orderItem.getValue().getItemAmount().get();

                if (counter.containsKey(itemID)) {
                    double amountTmp = counter.get(itemID);
                    counter.put(itemID, itemAmount + amountTmp);
                } else {
                    counter.put(itemID, itemAmount);
                }
            }
        }

        for (int id : counter.keySet()) {
            harvestPlan.add(new ItemCounter(itemDataMap.get(id), counter.get(id)));
        }
        return harvestPlan;
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
    void applyAllFilters(ActionEvent event) {
        OrderFilterList filterList = new OrderFilterList();

        if (filter_customer_checkbox.isSelected()) {
            OrderCustomerFilter customerFilter = new OrderCustomerFilter(filter_customer.getValue());
            filterList.add(customerFilter);
        }

        if (filter_date_checkbox.isSelected()) {
            OrderDateFilter dateFilter = null;
            try {
                dateFilter = new OrderDateFilter(filter_start_date.getValue(), filter_end_date.getValue());
            }
            catch (InvalidDateRangeException e) {
                Warning.display("Unzulässige Daten");
            }
            filterList.add(dateFilter);
        }

        showFiltered(filterList);

    }

    void showFiltered(OrderFilterList orderFilterList) {

        rootOrder = new TreeItem<>();

        for (TreeItem<OrderData> o : orderDataMap.values()) {

            boolean show=true;

            for (OrderFilter filter : orderFilterList.get()) {
                if (!filter.checkCondition(o.getValue())) show=false;
            }

            if (show) rootOrder.getChildren().add(o);

        }

        orderTable.setRoot(rootOrder);
        orderTableDeliveryDate.setSortType(TreeTableColumn.SortType.ASCENDING);
        orderTable.getSortOrder().add(orderTableDeliveryDate);
    }


    @FXML
    void newOrder(ActionEvent event) {

        orderBox.setVisible(true);


        itemCounters =FXCollections.observableArrayList();
        addTable.setItems(null);

        updateAddTable();
    }

    @FXML
    void itemQuantityPressed(ActionEvent event) {
        addOrderEntry(event);
    }

    @FXML
    void itemSelected(ActionEvent event) {
        amountField.requestFocus();
    }

    @FXML
    void setToday(ActionEvent event) {
        filter_start_date.setValue(LocalDate.now());
        filter_end_date.setValue(LocalDate.now());
    }

    @FXML
    void setTomorrow(ActionEvent event) {
        filter_start_date.setValue(LocalDate.now().plusDays(1));
        filter_end_date.setValue(LocalDate.now().plusDays(1));
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

        date_filter_box.setVisible(false);
        filter_customer.setVisible(false);

        datePicker.setConverter(
                new StringConverter<LocalDate>() {
                    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd. MM. yyyy");
                    @Override
                    public String toString(LocalDate date) {
                        return (date != null) ? dateFormatter.format(date) : "";
                    }

                    @Override
                    public LocalDate fromString(String string) {
                        return (string != null && !string.isEmpty())
                                ? LocalDate.parse(string, dateFormatter)
                                : null;
                    }
                }
        );

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




}
