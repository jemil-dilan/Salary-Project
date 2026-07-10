package com.salaryvalidation.controller;

import com.salaryvalidation.application.salary.GetStatisticsUseCase;
import com.salaryvalidation.infrastructure.api.DashboardApi;
import com.salaryvalidation.infrastructure.api.model.StatisticsResponse;
import com.salaryvalidation.infrastructure.api.model.StatisticsResponseAmounts;
import com.salaryvalidation.infrastructure.api.model.StatisticsResponseConfirmations;
import com.salaryvalidation.infrastructure.api.model.StatisticsResponseEmployees;
import com.salaryvalidation.infrastructure.api.model.StatisticsResponsePeriod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
public class DashboardController implements DashboardApi {

    private final GetStatisticsUseCase getStatisticsUseCase;

    public DashboardController(GetStatisticsUseCase getStatisticsUseCase) {
        this.getStatisticsUseCase = getStatisticsUseCase;
    }

    @Override
    public ResponseEntity<StatisticsResponse> getStatistics(Integer month, Integer year) {
        GetStatisticsUseCase.DashboardStatisticsResponse stats = getStatisticsUseCase.execute(month, year);

        StatisticsResponse response = new StatisticsResponse();

        StatisticsResponsePeriod period = new StatisticsResponsePeriod();
        period.setMonth(stats.period().month());
        period.setYear(stats.period().year());
        response.setPeriod(period);

        StatisticsResponseEmployees employees = new StatisticsResponseEmployees();
        employees.setTotal((int) stats.employees().total());
        employees.setActive((int) stats.employees().active());
        employees.setWithPaymentRegistered((int) stats.employees().withPaymentRegistered());
        employees.setWithoutPaymentRegistered((int) stats.employees().withoutPaymentRegistered());
        response.setEmployees(employees);

        StatisticsResponseConfirmations confirmations = new StatisticsResponseConfirmations();
        confirmations.setPending((int) stats.confirmations().pending());
        confirmations.setFullyReceived((int) stats.confirmations().fullyReceived());
        confirmations.setPartiallyReceived((int) stats.confirmations().partiallyReceived());
        confirmations.setNotReceived((int) stats.confirmations().notReceived());
        response.setConfirmations(confirmations);

        StatisticsResponseAmounts amounts = new StatisticsResponseAmounts();
        amounts.setTotalExpected(toDouble(stats.amounts().totalExpected()));
        amounts.setTotalReceived(toDouble(stats.amounts().totalReceived()));
        amounts.setTotalOutstanding(toDouble(stats.amounts().totalOutstanding()));
        response.setAmounts(amounts);

        return ResponseEntity.ok(response);
    }

    private Double toDouble(BigDecimal value) {
        if (value == null) {
            return 0.0;
        }
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
