package com.salaryvalidation.controller;

import com.salaryvalidation.AbstractIntegrationTest;
import com.salaryvalidation.infrastructure.api.model.CreateEmployeeRequest;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmployeeJpaRepository employeeRepo;

    private static UUID createdEmployeeId;

    @Test
    @Order(1)
    void shouldCreateEmployeeSuccessfully() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setMatricule("EMP-TEST-001");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setDepartment("Engineering");
        request.setPosition("Developer");

        createdEmployeeId = given()
            .body(request)
            .when()
            .post("/employees")
            .then()
            .statusCode(201)
            .body("matricule", equalTo("EMP-TEST-001"))
            .body("firstName", equalTo("Test"))
            .body("lastName", equalTo("User"))
            .body("active", equalTo(true))
            .body("id", notNullValue())
            .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void shouldFailCreateEmployee_WhenMatriculeDuplicatesSeedData() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setMatricule("EMP-001");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDepartment("Engineering");
        request.setPosition("Developer");

        given()
            .body(request)
            .when()
            .post("/employees")
            .then()
            .statusCode(409)
            .body("errorCode", equalTo("DUPLICATE_MATRICULE"));
    }

    @Test
    @Order(3)
    void shouldGetAllEmployeesWithPagination() {
        given()
            .queryParam("page", 1)
            .queryParam("size", 5)
            .when()
            .get("/employees")
            .then()
            .statusCode(200)
            .body("pagination.size", equalTo(5))
            .body("content", isA(java.util.List.class));
    }

    @Test
    @Order(4)
    void shouldGetEmployeeByIdSuccessfully() {
        given()
            .when()
            .get("/employees/{id}", createdEmployeeId)
            .then()
            .statusCode(200)
            .body("firstName", equalTo("Test"))
            .body("matricule", equalTo("EMP-TEST-001"));
    }

    @Test
    @Order(5)
    void shouldFailGetEmployee_WhenNotFound() {
        given()
            .when()
            .get("/employees/{id}", "00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404)
            .body("errorCode", equalTo("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    @Order(6)
    void shouldSearchByMatricule_WithCurrentPayment() {
        given()
            .when()
            .get("/employees/matricule/{matricule}", "EMP-001")
            .then()
            .statusCode(200)
            .body("employee.matricule", equalTo("EMP-001"))
            .body("salaryPayment", notNullValue())
            .body("salaryPayment.status", equalTo("FULLY_RECEIVED"));
    }

    @Test
    @Order(7)
    void shouldSearchByMatricule_WithoutCurrentPayment() {
        given()
            .when()
            .get("/employees/matricule/{matricule}", "EMP-004")
            .then()
            .statusCode(200)
            .body("employee.matricule", equalTo("EMP-004"))
            .body("salaryPayment", nullValue());
    }

    @Test
    @Order(8)
    void shouldFailSearchByMatricule_WhenEmployeeInactive() {
        given()
            .when()
            .get("/employees/matricule/{matricule}", "EMP-005")
            .then()
            .statusCode(403)
            .body("errorCode", equalTo("EMPLOYEE_INACTIVE"));
    }

    @Test
    @Order(9)
    void shouldFailSearchByMatricule_WhenNotFound() {
        given()
            .when()
            .get("/employees/matricule/{matricule}", "INVALID-999")
            .then()
            .statusCode(404)
            .body("errorCode", equalTo("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    @Order(10)
    void shouldUpdateEmployeeSuccessfully() {
        CreateEmployeeRequest updateRequest = new CreateEmployeeRequest();
        updateRequest.setMatricule("EMP-TEST-001");
        updateRequest.setFirstName("Test");
        updateRequest.setLastName("User");
        updateRequest.setDepartment("Engineering");
        updateRequest.setPosition("Senior Developer");

        given()
            .body(updateRequest)
            .when()
            .put("/employees/{id}", createdEmployeeId)
            .then()
            .statusCode(200)
            .body("position", equalTo("Senior Developer"))
            .body("firstName", equalTo("Test"));
    }

    @Test
    @Order(11)
    void shouldSoftDeleteEmployeeSuccessfully() {
        given()
            .when()
            .delete("/employees/{id}", createdEmployeeId)
            .then()
            .statusCode(204);

        var entity = employeeRepo.findById(createdEmployeeId)
            .orElseThrow(() -> new AssertionError("Employee should still exist after soft delete"));
        assert !entity.isActive() : "Employee should be deactivated after soft delete";
    }
}
