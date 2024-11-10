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

    public String getName() {
        return name;
    }

    // 현재 날짜에 유효한 프로모션인지 확인하는 메서드
    public boolean isActive(LocalDate date) {
        return (date.isEqual(startDate) || date.isAfter(startDate)) &&
                (date.isEqual(endDate) || date.isBefore(endDate));
    }
}
