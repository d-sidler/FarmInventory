package order;

import java.util.ArrayList;

public class OrderFilterList {

    private ArrayList<OrderFilter> filters;

    public OrderFilterList(OrderFilter... orderfilters) {
        filters = new ArrayList<>();

       for (OrderFilter f : orderfilters) {
           this.filters.add(f);
       }
    }

    public ArrayList<OrderFilter> get() {
        return filters;
    }

    public void add(OrderFilter orderFilter) {
        filters.add(orderFilter);
    }


}
