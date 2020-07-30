package order;

import java.time.LocalDate;

public class OrderDateFilter extends OrderFilter {

    private LocalDate startDate;
    private LocalDate endDate;

    public OrderDateFilter(LocalDate startDate, LocalDate endDate) throws InvalidDateRangeException {
        this.startDate = startDate.minusDays(1);
        this.endDate = endDate.plusDays(1);

        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException();
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException();
        }
    }

    @Override
    public boolean checkCondition(OrderData orderData) {

        LocalDate harvestDate = orderData.getHarvestDate().get().toLocalDate();
        boolean isInDateRange = harvestDate.isAfter(startDate) && harvestDate.isBefore(endDate);
        return isInDateRange;

    }
}
