package com.salaryvalidation.application.salary;

import com.salaryvalidation.domain.salary.PaymentStatus;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class GetStatisticsUseCase {

    @PersistenceContext
    private EntityManager entityManager;

    public record DashboardStatisticsResponse(
        PeriodInfo period,
        EmployeeCounts employees,
        ConfirmationCounts confirmations,
        Amounts amounts,
        long unresolvedCases,
        double confirmationRate
    ) {
        public record PeriodInfo(int month, int year) {}
        public record EmployeeCounts(long total, long active, long withPaymentRegistered, long withoutPaymentRegistered) {}
        public record ConfirmationCounts(long pending, long fullyReceived, long partiallyReceived, long notReceived) {}
        public record Amounts(BigDecimal totalExpected, BigDecimal totalReceived, BigDecimal totalOutstanding) {}
    }

    public DashboardStatisticsResponse execute(Integer month, Integer year) {
        YearMonth now = YearMonth.now(ZoneOffset.UTC);
        int statMonth = month != null ? month : now.getMonthValue();
        int statYear = year != null ? year : now.getYear();

        long totalEmployees = entityManager.createQuery(
            "SELECT COUNT(e) FROM EmployeeEntity e", Long.class).getSingleResult();

        long activeEmployees = entityManager.createQuery(
            "SELECT COUNT(e) FROM EmployeeEntity e WHERE e.active = true", Long.class).getSingleResult();

        TypedQuery<SalaryPaymentEntity> paymentQuery = entityManager.createQuery(
            "SELECT sp FROM SalaryPaymentEntity sp WHERE sp.month = :month AND sp.year = :year",
            SalaryPaymentEntity.class);
        paymentQuery.setParameter("month", statMonth);
        paymentQuery.setParameter("year", statYear);
        List<SalaryPaymentEntity> payments = paymentQuery.getResultList();

        long withPayment = 0;
        long pending = 0;
        long fullyReceived = 0;
        long partiallyReceived = 0;
        long notReceived = 0;
        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;

        for (SalaryPaymentEntity p : payments) {
            withPayment++;
            totalExpected = totalExpected.add(p.getExpectedAmount());

            switch (p.getStatus()) {
                case PENDING -> pending++;
                case FULLY_RECEIVED -> {
                    fullyReceived++;
                    if (p.getReceivedAmount() != null) {
                        totalReceived = totalReceived.add(p.getReceivedAmount());
                    }
                }
                case PARTIALLY_RECEIVED -> {
                    partiallyReceived++;
                    if (p.getReceivedAmount() != null) {
                        totalReceived = totalReceived.add(p.getReceivedAmount());
                    }
                }
                case NOT_RECEIVED -> {
                    notReceived++;
                }
            }
        }

        long withoutPayment = activeEmployees - withPayment;
        BigDecimal totalOutstanding = totalExpected.subtract(totalReceived);

        long unresolvedCases = partiallyReceived + notReceived;

        double confirmationRate = 0.0;
        if (withPayment > 0) {
            confirmationRate = BigDecimal.valueOf((withPayment - pending) * 100.0 / withPayment)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        }

        var period = new DashboardStatisticsResponse.PeriodInfo(statMonth, statYear);
        var empCounts = new DashboardStatisticsResponse.EmployeeCounts(
            totalEmployees, activeEmployees, withPayment, withoutPayment);
        var confCounts = new DashboardStatisticsResponse.ConfirmationCounts(
            pending, fullyReceived, partiallyReceived, notReceived);
        var amts = new DashboardStatisticsResponse.Amounts(totalExpected, totalReceived, totalOutstanding);

        return new DashboardStatisticsResponse(period, empCounts, confCounts, amts, unresolvedCases, confirmationRate);
    }
}
