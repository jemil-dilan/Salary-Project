package com.salaryvalidation.controller;

import com.salaryvalidation.AbstractIntegrationTest;
import com.salaryvalidation.infrastructure.api.model.CreateEmployeeRequest;
import com.salaryvalidation.infrastructure.api.model.CreateSalaryPaymentRequest;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SalaryPaymentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeJpaRepository employeeRepo;

    @Autowired
    private SalaryPaymentJpaRepository salaryPaymentRepo;

    @Test
    @Order(1)
    void shouldCreateSingleSalaryPaymentSuccessfully() {
        UUID emp004Id = employeeRepo.findByMatricule("EMP-004")
            .orElseThrow().getId();

        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        CreateSalaryPaymentRequest request = new CreateSalaryPaymentRequest(
            emp004Id, current.getMonthValue(), current.getYear(), 100000.0);

        given()
            .body(request)
            .when()
            .post("/salary-payments")
            .then()
            .statusCode(201)
            .body("status", equalTo("PENDING"))
            .body("employee.id", equalTo(emp004Id.toString()));
    }

    @Test
    @Order(2)
    void shouldFailCreateSinglePayment_WhenDuplicateForPeriod() {
        UUID emp001Id = employeeRepo.findByMatricule("EMP-001")
            .orElseThrow().getId();

        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        CreateSalaryPaymentRequest request = new CreateSalaryPaymentRequest(
            emp001Id, current.getMonthValue(), current.getYear(), 150000.0);

        given()
            .body(request)
            .when()
            .post("/salary-payments")
            .then()
            .statusCode(409)
            .body("errorCode", equalTo("DUPLICATE_SALARY_PAYMENT"));
    }

    @Test
    @Order(3)
    void shouldFailCreateSinglePayment_WhenEmployeeInactive() {
        UUID emp005Id = employeeRepo.findByMatricule("EMP-005")
            .orElseThrow().getId();

        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        CreateSalaryPaymentRequest request = new CreateSalaryPaymentRequest(
            emp005Id, current.getMonthValue(), current.getYear(), 100000.0);

        given()
            .body(request)
            .when()
            .post("/salary-payments")
            .then()
            .statusCode(404)
            .body("errorCode", equalTo("EMPLOYEE_NOT_FOUND"));
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(4)
    void shouldBulkCreateSalaryPaymentsSuccessfully() {
        String emp1Id = createEmployee("EMP-BULK-001");
        String emp2Id = createEmployee("EMP-BULK-002");

        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        Map<String, Object> body = Map.of(
            "month", current.getMonthValue(),
            "year", current.getYear(),
            "payments", List.of(
                Map.of("employeeId", emp1Id, "expectedAmount", 100000.0),
                Map.of("employeeId", emp2Id, "expectedAmount", 120000.0)
            )
        );

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/salary-payments/bulk")
            .then()
            .statusCode(201)
            .body("createdCount", equalTo(2));
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(5)
    void shouldFailBulkCreate_AndRollbackTransaction() {
        String emp1Id = createEmployee("EMP-BULK-003");
        String emp2Id = createEmployee("EMP-BULK-004");

        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        Map<String, Object> body = Map.of(
            "month", current.getMonthValue(),
            "year", current.getYear(),
            "payments", List.of(
                Map.of("employeeId", emp1Id, "expectedAmount", 100000.0),
                Map.of("employeeId", emp2Id, "expectedAmount", -1.0)
            )
        );

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/salary-payments/bulk")
            .then()
            .statusCode(greaterThanOrEqualTo(400));

        boolean hasPaymentForEmp1 = salaryPaymentRepo
            .findByEmployeeIdAndMonthAndYear(
                UUID.fromString(emp1Id), current.getMonthValue(), current.getYear())
            .isPresent();
        boolean hasPaymentForEmp2 = salaryPaymentRepo
            .findByEmployeeIdAndMonthAndYear(
                UUID.fromString(emp2Id), current.getMonthValue(), current.getYear())
            .isPresent();

        assert !hasPaymentForEmp1 : "Emp1 should have no payment after rollback";
        assert !hasPaymentForEmp2 : "Emp2 should have no payment after rollback";
    }

    private String createEmployee(String matricule) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setMatricule(matricule);
        request.setFirstName("Bulk");
        request.setLastName("Test");
        request.setDepartment("Engineering");
        request.setPosition("Developer");

        return given()
            .body(request)
            .when()
            .post("/employees")
            .then()
            .statusCode(201)
            .extract()
            .path("id")
            .toString();
    }
}
