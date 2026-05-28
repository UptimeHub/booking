package uz.uptimehub.booking.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseIntegrationTestSmokeTest extends BaseIntegrationTest {

    @Test
    void contextLoadsWithIntegrationTestDependencies() {
        assertThat(port).isPositive();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
        assertThat(resourceServiceMock.isRunning()).isTrue();
    }
}
