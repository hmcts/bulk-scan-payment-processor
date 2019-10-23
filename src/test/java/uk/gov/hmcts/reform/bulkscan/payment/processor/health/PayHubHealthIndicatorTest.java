package uk.gov.hmcts.reform.bulkscan.payment.processor.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayHubHealthIndicatorTest {

    @Mock
    private RestTemplate restTemplate;

    private static final String PAY_HUB_URI = "http://localhost:8080";

    @Mock
    private ResponseEntity<Object> mockResponseEntity;

    private HealthIndicator payHubHealthIndicator;

    @BeforeEach
    void setUp() {
        payHubHealthIndicator = new PayHubHealthIndicator(PAY_HUB_URI, restTemplate);
    }

    @Test
    void should_be_healthy_when_pay_hub_health_check_status_is_ok() {
        // given
        when(restTemplate.getForEntity(PAY_HUB_URI + "/health", Object.class)).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        // then
        assertThat(payHubHealthIndicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void should_be_unhealthy_when_pay_hub_health_check_status_is_not_ok() {
        // given
        when(restTemplate.getForEntity(PAY_HUB_URI + "/health", Object.class)).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        // then
        assertThat(payHubHealthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void should_be_unhealthy_when_pay_hub_health_check_throws_exception() {
        // given
        when(restTemplate.getForEntity(PAY_HUB_URI + "/health", Object.class)).thenThrow(RuntimeException.class);
        // then
        assertThat(payHubHealthIndicator.health().getStatus()).isEqualTo(Status.UNKNOWN);
    }
}
