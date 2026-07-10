package com.salaryvalidation.application.salary;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.domain.salary.PaymentStatus;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.mapper.SalaryPaymentPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListSalaryPaymentsUseCase {

    @PersistenceContext
    private EntityManager entityManager;

    private final SalaryPaymentPersistenceMapper salaryPaymentMapper;
    private final EmployeePersistenceMapper employeeMapper;

    public ListSalaryPaymentsUseCase(SalaryPaymentPersistenceMapper salaryPaymentMapper,
                                      EmployeePersistenceMapper employeeMapper) {
        this.salaryPaymentMapper = salaryPaymentMapper;
        this.employeeMapper = employeeMapper;
    }

    public record SalaryPaymentWithEmployeeResponse(SalaryPayment salaryPayment, Employee employee) {
    }

    public Page<SalaryPaymentWithEmployeeResponse> execute(Integer month, Integer year,
                                                            String status, UUID employeeId,
                                                            Pageable pageable) {
        List<String> conditions = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();

        if (month != null) {
            conditions.add("sp.month = :month");
            paramNames.add("month");
            paramValues.add(month);
        }
        if (year != null) {
            conditions.add("sp.year = :year");
            paramNames.add("year");
            paramValues.add(year);
        }
        if (status != null) {
            conditions.add("sp.status = :status");
            paramNames.add("status");
            paramValues.add(PaymentStatus.valueOf(status));
        }
        if (employeeId != null) {
            conditions.add("sp.employee.id = :employeeId");
            paramNames.add("employeeId");
            paramValues.add(employeeId);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        TypedQuery<SalaryPaymentEntity> query = entityManager.createQuery(
            "SELECT sp FROM SalaryPaymentEntity sp" + whereClause + " ORDER BY sp.year DESC, sp.month DESC",
            SalaryPaymentEntity.class);

        TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(sp) FROM SalaryPaymentEntity sp" + whereClause, Long.class);

        for (int i = 0; i < paramNames.size(); i++) {
            query.setParameter(paramNames.get(i), paramValues.get(i));
            countQuery.setParameter(paramNames.get(i), paramValues.get(i));
        }

        long total = countQuery.getSingleResult();

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<SalaryPaymentWithEmployeeResponse> content = query.getResultList().stream()
            .map(e -> {
                SalaryPayment sp = salaryPaymentMapper.toDomain(e);
                Employee emp = employeeMapper.toDomain(e.getEmployee());
                return new SalaryPaymentWithEmployeeResponse(sp, emp);
            })
            .toList();

        return new PageImpl<>(content, pageable, total);
    }
}
