package com.salaryvalidation.controller;

import com.salaryvalidation.application.employee.CreateEmployeeUseCase;
import com.salaryvalidation.application.employee.DeleteEmployeeUseCase;
import com.salaryvalidation.application.employee.GetEmployeeUseCase;
import com.salaryvalidation.application.employee.ListEmployeesUseCase;
import com.salaryvalidation.application.employee.SearchByMatriculeUseCase;
import com.salaryvalidation.application.employee.UpdateEmployeeUseCase;
import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.infrastructure.api.EmployeesApi;
import com.salaryvalidation.infrastructure.api.model.CreateEmployeeRequest;
import com.salaryvalidation.infrastructure.api.model.EmployeeRef;
import com.salaryvalidation.infrastructure.api.model.EmployeeResponse;
import com.salaryvalidation.infrastructure.api.model.EmployeeWithSalaryResponse;
import com.salaryvalidation.infrastructure.api.model.PagedEmployeesResponse;
import com.salaryvalidation.infrastructure.api.model.PagedSalaryHistoryResponse;
import com.salaryvalidation.infrastructure.api.model.SalaryPaymentSimple;
import com.salaryvalidation.mapper.EmployeeDtoMapper;
import com.salaryvalidation.mapper.SalaryPaymentDtoMapper;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class EmployeeController implements EmployeesApi {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final DeleteEmployeeUseCase deleteEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final SearchByMatriculeUseCase searchByMatriculeUseCase;
    private final EmployeeDtoMapper employeeDtoMapper;
    private final SalaryPaymentDtoMapper salaryPaymentDtoMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase,
                              UpdateEmployeeUseCase updateEmployeeUseCase,
                              DeleteEmployeeUseCase deleteEmployeeUseCase,
                              GetEmployeeUseCase getEmployeeUseCase,
                              ListEmployeesUseCase listEmployeesUseCase,
                              SearchByMatriculeUseCase searchByMatriculeUseCase,
                              EmployeeDtoMapper employeeDtoMapper,
                              SalaryPaymentDtoMapper salaryPaymentDtoMapper) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.deleteEmployeeUseCase = deleteEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.searchByMatriculeUseCase = searchByMatriculeUseCase;
        this.employeeDtoMapper = employeeDtoMapper;
        this.salaryPaymentDtoMapper = salaryPaymentDtoMapper;
    }

    @Override
    public ResponseEntity<EmployeeResponse> createEmployee(CreateEmployeeRequest createEmployeeRequest) {
        Employee employee = createEmployeeUseCase.execute(createEmployeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeDtoMapper.toResponse(employee));
    }

    @Override
    public ResponseEntity<PagedEmployeesResponse> listEmployees(Integer page, Integer size,
                                                                  String sort, Boolean active,
                                                                  String department) {
        Sort sorting = parseSort(sort, "lastName", "asc");
        Pageable pageable = PageRequest.of(page - 1, Math.min(size, 100), sorting);

        Page<Employee> employeePage = listEmployeesUseCase.execute(active, department, pageable);

        PagedEmployeesResponse response = new PagedEmployeesResponse();
        response.setContent(employeePage.getContent().stream()
            .map(employeeDtoMapper::toResponse)
            .toList());
        response.setPagination(PaginationUtil.toPagination(employeePage));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<EmployeeResponse> getEmployee(UUID employeeId) {
        Employee employee = getEmployeeUseCase.execute(employeeId);
        return ResponseEntity.ok(employeeDtoMapper.toResponse(employee));
    }

    @Override
    public ResponseEntity<EmployeeWithSalaryResponse> searchByMatricule(String matricule) {
        SearchByMatriculeUseCase.EmployeeWithCurrentPaymentResponse result =
            searchByMatriculeUseCase.execute(matricule);

        EmployeeWithSalaryResponse response = new EmployeeWithSalaryResponse();
        response.setEmployee(employeeDtoMapper.toResponse(result.employee()));

        if (result.salaryPayment() != null) {
            SalaryPaymentSimple simple = salaryPaymentDtoMapper.toSimpleResponse(result.salaryPayment());
            simple.setCanConfirm(result.canConfirm());
            response.setSalaryPayment(simple);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<EmployeeResponse> updateEmployee(UUID employeeId,
                                                            CreateEmployeeRequest createEmployeeRequest) {
        Employee employee = updateEmployeeUseCase.execute(employeeId, createEmployeeRequest);
        return ResponseEntity.ok(employeeDtoMapper.toResponse(employee));
    }

    @Override
    public ResponseEntity<Void> deleteEmployee(UUID employeeId) {
        deleteEmployeeUseCase.execute(employeeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PagedSalaryHistoryResponse> getEmployeeSalaryHistory(
            UUID employeeId, Integer page, Integer size, String sort, Integer year) {
        Sort sorting = parseSort(sort, "year", "desc");
        sorting = sorting.and(Sort.by(Sort.Direction.DESC, "month"));
        Pageable pageable = PageRequest.of(page - 1, Math.min(size, 100), sorting);

        TypedQuery<SalaryPaymentEntity> query;
        TypedQuery<Long> countQuery;

        if (year != null) {
            query = entityManager.createQuery(
                "SELECT sp FROM SalaryPaymentEntity sp WHERE sp.employee.id = :employeeId AND sp.year = :year ORDER BY sp.year DESC, sp.month DESC",
                SalaryPaymentEntity.class);
            query.setParameter("employeeId", employeeId);
            query.setParameter("year", year);

            countQuery = entityManager.createQuery(
                "SELECT COUNT(sp) FROM SalaryPaymentEntity sp WHERE sp.employee.id = :employeeId AND sp.year = :year",
                Long.class);
            countQuery.setParameter("employeeId", employeeId);
            countQuery.setParameter("year", year);
        } else {
            query = entityManager.createQuery(
                "SELECT sp FROM SalaryPaymentEntity sp WHERE sp.employee.id = :employeeId ORDER BY sp.year DESC, sp.month DESC",
                SalaryPaymentEntity.class);
            query.setParameter("employeeId", employeeId);

            countQuery = entityManager.createQuery(
                "SELECT COUNT(sp) FROM SalaryPaymentEntity sp WHERE sp.employee.id = :employeeId",
                Long.class);
            countQuery.setParameter("employeeId", employeeId);
        }

        long total = countQuery.getSingleResult();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<SalaryPaymentEntity> entities = query.getResultList();

        List<SalaryPaymentSimple> content = entities.stream()
            .map(e -> {
                SalaryPayment domain = new SalaryPayment();
                domain.setId(e.getId());
                domain.setEmployeeId(e.getEmployee().getId());
                domain.setMonth(e.getMonth());
                domain.setYear(e.getYear());
                domain.setExpectedAmount(e.getExpectedAmount());
                domain.setReceivedAmount(e.getReceivedAmount());
                domain.setStatus(e.getStatus());
                domain.setConfirmationDate(e.getConfirmationDate());
                return salaryPaymentDtoMapper.toSimpleResponse(domain);
            })
            .toList();

        Page<SalaryPaymentSimple> salaryPage = new PageImpl<>(content, pageable, total);
        PagedSalaryHistoryResponse response = new PagedSalaryHistoryResponse();
        response.setContent(content);
        response.setPagination(PaginationUtil.toPagination(salaryPage));

        return ResponseEntity.ok(response);
    }

    private Sort parseSort(String sort, String defaultField, String defaultDirection) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.fromString(defaultDirection), defaultField);
        }
        String[] parts = sort.split(",");
        String field = parts[0];
        String dir = parts.length > 1 ? parts[1] : defaultDirection;
        return Sort.by(Sort.Direction.fromString(dir), field);
    }
}
