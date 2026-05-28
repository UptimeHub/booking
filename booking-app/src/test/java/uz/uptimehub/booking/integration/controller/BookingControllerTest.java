package uz.uptimehub.booking.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import uz.uptimehub.booking.integration.BaseIntegrationTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class BookingControllerTest extends BaseIntegrationTest {

    private static final String CREATE_BOOKING_JSON_PATH = "json/booking/controller/create/";
    private static final UUID RESOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldCreateBookingWithPendingStatus() throws IOException {
        String requestBody = readJson(CREATE_BOOKING_JSON_PATH + "request.json");
        JsonNode expectedResponse = readJsonNode(CREATE_BOOKING_JSON_PATH + "expected-response.json");

        resourceServiceMock.stubFor(get(urlEqualTo("/api/resource/" + RESOURCE_ID))
                .willReturn(okJson(readJson(CREATE_BOOKING_JSON_PATH + "resource-response.json"))));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", USER_ID.toString());

        ResponseEntity<String> response = RestClient.create("http://localhost:" + port)
                .post()
                .uri("/api/booking")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(stableResponseFields(response.getBody())).isEqualTo(expectedResponse);
    }

    private JsonNode stableResponseFields(String responseBody) throws IOException {
        JsonNode response = OBJECT_MAPPER.readTree(responseBody);
        ObjectNode stableFields = OBJECT_MAPPER.createObjectNode();
        stableFields.set("resourceId", response.get("resourceId"));
        stableFields.set("organizationId", response.get("organizationId"));
        stableFields.set("userId", response.get("userId"));
        stableFields.set("startTime", response.get("startTime"));
        stableFields.set("endTime", response.get("endTime"));
        stableFields.set("status", response.get("status"));
        return stableFields;
    }

    private JsonNode readJsonNode(String path) throws IOException {
        return OBJECT_MAPPER.readTree(readJson(path));
    }

    private String readJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
