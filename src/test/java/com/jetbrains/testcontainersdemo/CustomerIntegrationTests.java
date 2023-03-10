package com.jetbrains.testcontainersdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class CustomerIntegrationTests {

    @Autowired
    CustomerDao customerDao;

    // use sth like GenericContainer container = new GenericContainer("myapp:v3")
    //      .withExposedPort(8081); <-- testcontainers waits until this port is Bound.
    // if you want to setup a custom container that e.g. runs some
    // other spring boot server, that this app connects to via (say a) FeignClient...
    // make classpath resource accessible in a container as:
    // > mySQLContainer.withClasspathResourceMapping("schema.sql", "/tmp/schem.sql", BindMode.READ_ONLY)
    // similar: container.withFileSystemBinding("src", "trg", BindMode.READ_WRITE) <-- container can write..
    // better:    container.copyFileToContainer(); or ...FromContainer
    // also: container.execInContainer("ls", "-la");
    // container.getLogs(..) -> access to logs
    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:latest");

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
