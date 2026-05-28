package uz.uptimehub.booking.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    protected static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18.3-alpine")
                    .withDatabaseName("booking_test_db")
                    .withUsername("test")
                    .withPassword("test");

    protected static final KafkaContainer kafka =
            new KafkaContainer("apache/kafka-native:3.8.0");

    protected static final WireMockServer resourceServiceMock = new WireMockServer(0);

    static {
        postgres.start();
        kafka.start();
        resourceServiceMock.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            resourceServiceMock.stop();
            kafka.stop();
            postgres.stop();
        }));
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("services.resource.url",
                resourceServiceMock::baseUrl);
    }
}
