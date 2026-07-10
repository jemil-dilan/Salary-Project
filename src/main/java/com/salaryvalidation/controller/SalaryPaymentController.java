package com.salaryvalidation.controller;

import com.salaryvalidation.application.salary.BulkCreateSalaryPaymentsUseCase;
import com.salaryvalidation.application.salary.ConfirmSalaryUseCase;
import com.salaryvalidation.application.salary.CreateSalaryPaymentUseCase;
import com.salaryvalidation.application.salary.GetSalaryPaymentUseCase;
import com.salaryvalidation.application.salary.ListSalaryPaymentsUseCase;
import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.infrastructure.api.SalaryPaymentsApi;
import com.salaryvalidation.infrastructure.api.model.BulkCreateSalaryPaymentRequest;
import com.salaryvalidation.infrastructure.api.model.BulkCreateSalaryPaymentResponse;
import com.salaryvalidation.infrastructure.api.model.ConfirmSalaryRequest;
import com.salaryvalidation.infrastructure.api.model.ConfirmationResponse;
import com.salaryvalidation.infrastructure.api.model.CreateSalaryPaymentRequest;
import com.salaryvalidation.infrastructure.api.model.EmployeeRef;
import com.salaryvalidation.infrastructure.api.model.PagedSalaryPaymentsResponse;
import com.salaryvalidation.infrastructure.api.model.SalaryPaymentResponse;
import com.salaryvalidation.mapper.SalaryPaymentDtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SalaryPaymentController implements SalaryPaymentsApi {

    private final CreateSalaryPaymentUseCase createSalaryPaymentUseCase;
    private final BulkCreateSalaryPaymentsUseCase bulkCreateSalaryPaymentsUseCase;
    private final GetSalaryPaymentUseCase getSalaryPaymentUseCase;
    private final ListSalaryPaymentsUseCase listSalaryPaymentsUseCase;
    private final ConfirmSalaryUseCase confirmSalaryUseCase;
    private final SalaryPaymentDtoMapper salaryPaymentDtoMapper;

    public SalaryPaymentController(CreateSalaryPaymentUseCase createSalaryPaymentUseCase,
                                    BulkCreateSalaryPaymentsUseCase bulkCreateSalaryPaymentsUseCase,
                                    GetSalaryPaymentUseCase getSalaryPaymentUseCase,
                                    ListSalaryPaymentsUseCase listSalaryPaymentsUseCase,
                                    ConfirmSalaryUseCase confirmSalaryUseCase,
                                    SalaryPaymentDtoMapper salaryPaymentDtoMapper) {
        this.createSalaryPaymentUseCase = createSalaryPaymentUseCase;
        this.bulkCreateSalaryPaymentsUseCase = bulkCreateSalaryPaymentsUseCase;
        this.getSalaryPaymentUseCase = getSalaryPaymentUseCase;
        this.listSalaryPaymentsUseCase = listSalaryPaymentsUseCase;
        this.confirmSalaryUseCase = confirmSalaryUseCase;
        this.salaryPaymentDtoMapper = salaryPaymentDtoMapper;
    }

    @Override
    public ResponseEntity<SalaryPaymentResponse> createSalaryPayment(
            CreateSalaryPaymentRequest createSalaryPaymentRequest) {
        SalaryPayment payment = createSalaryPaymentUseCase.execute(createSalaryPaymentRequest);
        EmployeeRef employeeRef = new EmployeeRef();
        employeeRef.setId(createSalaryPaymentRequest.getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salaryPaymentDtoMapper.toResponse(payment, employeeRef));
    }

    @Override
    public ResponseEntity<BulkCreateSalaryPaymentResponse> bulkCreateSalaryPayments(
            BulkCreateSalaryPaymentRequest bulkCreateSalaryPaymentRequest) {
        var payments = bulkCreateSalaryPaymentsUseCase.execute(bulkCreateSalaryPaymentRequest);

        BulkCreateSalaryPaymentResponse response = new BulkCreateSalaryPaymentResponse();
        response.setCreatedCount(payments.size());

        var paymentResponses = payments.stream()
            .map(p -> {
                EmployeeRef ref = new EmployeeRef();
                ref.setId(p.getEmployeeId());
                return salaryPaymentDtoMapper.toResponse(p, ref);
            })
            .toList();
        response.setPayments(paymentResponses);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<PagedSalaryPaymentsResponse> listSalaryPayments(
            Integer page, Integer size, String sort, Integer month, Integer year,
            com.salaryvalidation.infrastructure.api.model.PaymentStatus status, UUID employeeId) {

        Sort sorting = parseSort(sort, "year", "desc");
        sorting = sorting.and(Sort.by(Sort.Direction.DESC, "month"));
        Pageable pageable = PageRequest.of(page - 1, Math.min(size, 100), sorting);

        String statusStr = status != null ? status.getValue() : null;

        Page<ListSalaryPaymentsUseCase.SalaryPaymentWithEmployeeResponse> resultPage =
            listSalaryPaymentsUseCase.execute(month, year, statusStr, employeeId, pageable);

        PagedSalaryPaymentsResponse response = new PagedSalaryPaymentsResponse();
        response.setContent(resultPage.getContent().stream()
            .map(this::toSalaryPaymentResponse)
            .toList());
        response.setPagination(PaginationUtil.toPagination(resultPage));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SalaryPaymentResponse> getSalaryPayment(UUID paymentId) {
        GetSalaryPaymentUseCase.SalaryPaymentWithEmployeeResponse result =
            getSalaryPaymentUseCase.execute(paymentId);

        SalaryPaymentResponse response = toSalaryPaymentResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ConfirmationResponse> confirmSalary(
            String matricule, ConfirmSalaryRequest confirmSalaryRequest) {
        SalaryPayment payment = confirmSalaryUseCase.execute(matricule, confirmSalaryRequest);
        ConfirmationResponse response = salaryPaymentDtoMapper.toConfirmationResponse(payment);
        return ResponseEntity.ok(response);
    }

    private SalaryPaymentResponse toSalaryPaymentResponse(
            ListSalaryPaymentsUseCase.SalaryPaymentWithEmployeeResponse r) {
        EmployeeRef ref = new EmployeeRef();
        ref.setId(r.employee().getId());
        ref.setMatricule(r.employee().getMatricule());
        ref.setFirstName(r.employee().getFirstName());
        ref.setLastName(r.employee().getLastName());
        ref.setDepartment(r.employee().getDepartment());
        ref.setPosition(r.employee().getPosition());
        return salaryPaymentDtoMapper.toResponse(r.salaryPayment(), ref);
    }

    private SalaryPaymentResponse toSalaryPaymentResponse(
            GetSalaryPaymentUseCase.SalaryPaymentWithEmployeeResponse r) {
        EmployeeRef ref = new EmployeeRef();
        ref.setId(r.employee().getId());
        ref.setMatricule(r.employee().getMatricule());
        ref.setFirstName(r.employee().getFirstName());
        ref.setLastName(r.employee().getLastName());
        ref.setDepartment(r.employee().getDepartment());
        ref.setPosition(r.employee().getPosition());
        return salaryPaymentDtoMapper.toResponse(r.salaryPayment(), ref);
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
