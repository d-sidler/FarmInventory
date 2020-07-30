package order;

import customer.CustomerData;

public class OrderCustomerFilter extends OrderFilter {

    private CustomerData customer;
    private int customerID;

    public OrderCustomerFilter(CustomerData customer) {
        this.customerID = customer.id().get();
    }

    @Override
    public boolean checkCondition(OrderData orderData) {
        int orderCustomerID = orderData.getCustomerID().get();
        return customerID == orderCustomerID;
    }
}
