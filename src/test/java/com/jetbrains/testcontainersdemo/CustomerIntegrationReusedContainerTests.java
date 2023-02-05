package com.jetbrains.testcontainersdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CustomerIntegrationReusedContainerTests {

    @Autowired
    CustomerDao customerDao;

    // don't forget to opt in for reusable containers by creating a
    // ~/.testcontainers.properties with line: testcontainers.reuse.enable=true
    @SuppressWarnings("resource")
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:latest").withReuse(true);
    static {
        mySQLContainer.start();
    }

    @DynamicPropertySource
    static void overrideDatasourceProp(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @Test
    void when_using_a_clean_db_this_should_have_init_script_data() {
        List<Customer> customers = customerDao.findAll();
        assertThat(customers).hasSize(2);
    }
}
