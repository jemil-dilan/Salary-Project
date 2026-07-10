package com.salaryvalidation.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Employee Salary Validation Platform")
                .version("1.0")
                .description("A RESTful microservice that enables organizations to digitally " +
                    "monitor the confirmation of employee salary payments. The application does " +
                    "not calculate salaries, generate payroll, or execute bank transfers. Instead, " +
                    "it serves as a validation platform where employees confirm whether they have " +
                    "received their monthly salaries, allowing Human Resources (HR) to monitor " +
                    "payment status across the organization."));
    }
}
