package com.salaryvalidation.controller;

import com.salaryvalidation.AbstractIntegrationTest;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SalaryConfirmationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeJpaRepository employeeRepo;

    @Autowired
    private SalaryPaymentJpaRepository salaryPaymentRepo;

    private static final String TEST_MATRICULE = "EMP-CONFIRM-TEST";
    private static final double EXPECTED_AMOUNT = 100000.0;
    private EmployeeEntity testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new EmployeeEntity();
        testEmployee.setMatricule(TEST_MATRICULE);
        testEmployee.setFirstName("Confirm");
        testEmployee.setLastName("Test");
        testEmployee.setDepartment("QA");
        testEmployee.setPosition("Tester");
        testEmployee.setActive(true);
        testEmployee = employeeRepo.save(testEmployee);

        YearMonth current = YearMonth.now(ZoneOffset.UTC);
        SalaryPaymentEntity payment = new SalaryPaymentEntity();
        payment.setEmployee(testEmployee);
        payment.setMonth(current.getMonthValue());
        payment.setYear(current.getYear());
        payment.setExpectedAmount(BigDecimal.valueOf(EXPECTED_AMOUNT));
        payment.setStatus(com.salaryvalidation.domain.salary.PaymentStatus.PENDING);
        salaryPaymentRepo.save(payment);
    }

    @AfterEach
    void tearDown() {
        if (testEmployee != null) {
            salaryPaymentRepo.findByEmployeeIdAndMonthAndYear(
                    testEmployee.getId(),
                    YearMonth.now(ZoneOffset.UTC).getMonthValue(),
                    YearMonth.now(ZoneOffset.UTC).getYear())
                .ifPresent(p -> salaryPaymentRepo.delete(p));
            employeeRepo.delete(testEmployee);
        }
    }

    @Test
    @Order(1)
    void shouldDeriveFullyReceived_WhenAmountEqualsExpected() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "FULLY_RECEIVED", "receivedAmount", EXPECTED_AMOUNT))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200)
            .body("status", equalTo("FULLY_RECEIVED"))
            .body("receivedAmount", equalTo((float) EXPECTED_AMOUNT));
    }

    @Test
    @Order(2)
    void shouldDeriveNotReceived_WhenAmountIsNull() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "NOT_RECEIVED"))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200)
            .body("status", equalTo("NOT_RECEIVED"));
    }

    @Test
    @Order(3)
    void shouldDerivePartiallyReceived_WhenAmountIsLessThanExpected() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "PARTIALLY_RECEIVED", "receivedAmount", 50000.0))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200)
            .body("status", equalTo("PARTIALLY_RECEIVED"))
            .body("receivedAmount", equalTo(50000.0f));
    }

    @Test
    @Order(4)
    void shouldAllowIncrementalUpdate_WhenPartiallyReceived() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "PARTIALLY_RECEIVED", "receivedAmount", 30000.0))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200)
            .body("status", equalTo("PARTIALLY_RECEIVED"))
            .body("receivedAmount", equalTo(30000.0f));

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "PARTIALLY_RECEIVED", "receivedAmount", 70000.0))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200)
            .body("status", equalTo("PARTIALLY_RECEIVED"))
            .body("receivedAmount", equalTo(70000.0f));
    }

    @Test
    @Order(5)
    void shouldFailConfirmation_WhenAmountDecreased() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "PARTIALLY_RECEIVED", "receivedAmount", 50000.0))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "NOT_RECEIVED", "receivedAmount", 20000.0))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(400)
            .body("errorCode", equalTo("INVALID_AMOUNT_DECREASE"));
    }

    @Test
    @Order(6)
    void shouldFailConfirmation_WhenAlreadyConfirmed() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "FULLY_RECEIVED", "receivedAmount", EXPECTED_AMOUNT))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("status", "PARTIALLY_RECEIVED", "receivedAmount", 30000.0))
            .when()
            .patch("/employees/matricule/{matricule}/salary-payments/current/confirmation",
                TEST_MATRICULE)
            .then()
            .statusCode(409)
            .body("errorCode", equalTo("ALREADY_CONFIRMED"));
    }
}
