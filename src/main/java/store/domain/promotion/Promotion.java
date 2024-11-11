package store.domain.promotion;

import java.time.LocalDate;

public class Promotion {
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Promotion(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String nameValue() {
        return name;
    }

    public boolean isActive(LocalDate date) {
        return isAfterOrEqualStartDate(date) && isBeforeOrEqualEndDate(date);
    }

    private boolean isAfterOrEqualStartDate(LocalDate date) {
        return date.isEqual(startDate) || date.isAfter(startDate);
    }

    private boolean isBeforeOrEqualEndDate(LocalDate date) {
        return date.isEqual(endDate) || date.isBefore(endDate);
    }
}