package item;

import javafx.beans.property.*;

public class ItemData {

    private IntegerProperty id;
    private StringProperty itemName;
    private DoubleProperty price;
    private StringProperty priceUnit;
    private StringProperty priceUnitString;

    public ItemData(int id, String itemName, double price, String priceUnit) {
        this.id = new SimpleIntegerProperty(id);
        this.itemName = new SimpleStringProperty(itemName);
        this.price = new SimpleDoubleProperty(price);
        this.priceUnit = new SimpleStringProperty(priceUnit);

        this.priceUnitString = new SimpleStringProperty(" / " + priceUnit);
    }

    public IntegerProperty id() {
        return this.id;
    }

    public StringProperty itemName() {
        return this.itemName;
    }
    public String getItemName() {return this.itemName.get();}

    public DoubleProperty price() {
        return this.price;
    }
    public Double getPrice() {
        return this.price.get();
    }

    public StringProperty priceUnit() {
        return this.priceUnit;
    }
    public String getPriceUnit() {return this.priceUnit.get();}

    public StringProperty priceUnitString() {
        return this.priceUnitString;
    }

    public String getPriceUnitString() {
        return this.priceUnitString.get();
    }

    @Override
    public String toString() {
        return itemName.getValue();
    }

}
