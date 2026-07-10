package com.salaryvalidation.configuration;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class ConfirmationWindow {

    private final ConfirmationWindowProperties properties;
    private final Clock clock;

    public ConfirmationWindow(ConfirmationWindowProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public boolean isOpen() {
        LocalDate today = LocalDate.now(clock);
        int startDay = properties.getStartDay();
        int endDay = properties.getEndDay();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        LocalDate windowStart = LocalDate.of(currentYear, currentMonth, startDay);

        int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        int nextMonthYear = currentMonth == 12 ? currentYear + 1 : currentYear;
        LocalDate windowEnd = LocalDate.of(nextMonthYear, nextMonth, endDay);

        return !today.isBefore(windowStart) && !today.isAfter(windowEnd);
    }
}
