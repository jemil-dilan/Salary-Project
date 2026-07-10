package com.salaryvalidation.controller;

import com.salaryvalidation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Transactional
class DashboardControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldGetDashboardStatisticsForCurrentPeriod() {
        YearMonth current = YearMonth.now(ZoneOffset.UTC);

        given()
            .queryParam("month", current.getMonthValue())
            .queryParam("year", current.getYear())
            .when()
            .get("/dashboard/statistics")
            .then()
            .statusCode(200)
            .body("period.month", equalTo(current.getMonthValue()))
            .body("period.year", equalTo(current.getYear()))
            .body("employees.withPaymentRegistered", equalTo(3))
            .body("employees.withoutPaymentRegistered", equalTo(1))
            .body("confirmations.fullyReceived", equalTo(1))
            .body("confirmations.partiallyReceived", equalTo(1))
            .body("confirmations.pending", equalTo(1))
            .body("confirmations.notReceived", equalTo(0))
            .body("amounts.totalExpected", equalTo(450000.0f))
            .body("amounts.totalReceived", equalTo(260000.0f))
            .body("amounts.totalOutstanding", equalTo(190000.0f));
    }
}
