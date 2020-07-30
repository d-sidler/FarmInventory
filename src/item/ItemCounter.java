package item;

public class ItemCounter {
    private ItemData item;
    private Double amount;

    public ItemCounter(ItemData item, Double amount) {
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