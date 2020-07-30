package order;

import javafx.beans.property.*;

import java.sql.Date;

public class
OrderData {

    private IntegerProperty orderItemID;
    private IntegerProperty orderID;
    private IntegerProperty customerID;
    private IntegerProperty itemID;
    private DoubleProperty itemAmount;
    private ObjectProperty<Date> orderDate;
    private ObjectProperty<Date> harvestDate;
    private BooleanProperty finished;

    public OrderData(Integer orderItemID, Integer orderID, Integer customerID, Integer itemID, Double itemAmount, Date orderDate, Date harvestDate, boolean finished) {
        this.orderItemID = new SimpleIntegerProperty(orderItemID);
        this.orderID = new SimpleIntegerProperty(orderID);
        this.customerID = new SimpleIntegerProperty(customerID);
        this.itemID = new SimpleIntegerProperty(itemID);
        this.itemAmount = new SimpleDoubleProperty(itemAmount);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.harvestDate = new SimpleObjectProperty<>(harvestDate);
        this.finished = new SimpleBooleanProperty(finished);
    }

    public IntegerProperty getOrderItemID() {
        return orderItemID;
    }
    public IntegerProperty getOrderID() {return orderID;}
    public IntegerProperty getCustomerID() {return customerID;}
    public IntegerProperty getItemID()  {return itemID;}
    public DoubleProperty getItemAmount() {return itemAmount;}
    public ObjectProperty<Date> getOrderDate() {return orderDate;}
    public ObjectProperty<Date> getHarvestDate() {return harvestDate;}
    public BooleanProperty getFinished() {return finished;}



}
