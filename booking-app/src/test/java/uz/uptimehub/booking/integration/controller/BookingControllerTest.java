package uz.uptimehub.booking.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.integration.BaseIntegrationTest;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.booking.jpa.repository.BookingRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class BookingControllerTest extends BaseIntegrationTest {

    private static final String CREATE_BOOKING_JSON_PATH = "json/booking/controller/create/";
    private static final String GET_MY_BOOKINGS_JSON_PATH = "json/booking/controller/get-my-bookings/";
    private static final UUID RESOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_RESOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final UUID OTHER_RESOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private static final UUID ORGANIZATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333334");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create a booking with PENDING status when the resource is available")
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

    @Test
    @DisplayName("Should return bookings of a user")
    void shouldGetMyBookings() throws IOException {
        JsonNode expectedResponse = readJsonNode(GET_MY_BOOKINGS_JSON_PATH + "expected-response.json");
        bookingRepository.saveAll(List.of(
                booking(RESOURCE_ID, USER_ID, "2027-02-01T09:00:00", "2027-02-01T10:00:00", Status.PENDING),
                booking(SECOND_RESOURCE_ID, USER_ID, "2027-02-02T11:00:00", "2027-02-02T12:30:00", Status.ACTIVE),
                booking(OTHER_RESOURCE_ID, OTHER_USER_ID, "2027-02-03T09:00:00", "2027-02-03T10:00:00", Status.CANCELLED)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", USER_ID.toString());

        ResponseEntity<String> response = RestClient.create("http://localhost:" + port)
                .get()
                .uri("/api/booking/my?sort=startTime,asc")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(stablePageContentFields(response.getBody())).isEqualTo(expectedResponse);
    }

    private JsonNode stableResponseFields(String responseBody) throws IOException {
        JsonNode response = OBJECT_MAPPER.readTree(responseBody);
        return stableBookingFields(response);
    }

    private JsonNode stablePageContentFields(String responseBody) throws IOException {
        JsonNode content = OBJECT_MAPPER.readTree(responseBody).get("content");
        ArrayNode stableContent = OBJECT_MAPPER.createArrayNode();
        content.forEach(booking -> stableContent.add(stableBookingFields(booking)));
        return stableContent;
    }

    private JsonNode stableBookingFields(JsonNode response) {
        ObjectNode stableFields = OBJECT_MAPPER.createObjectNode();
        stableFields.set("resourceId", response.get("resourceId"));
        stableFields.set("organizationId", response.get("organizationId"));
        stableFields.set("userId", response.get("userId"));
        stableFields.set("startTime", response.get("startTime"));
        stableFields.set("endTime", response.get("endTime"));
        stableFields.set("status", response.get("status"));
        return stableFields;
    }

    private Booking booking(UUID resourceId, UUID userId, String startTime, String endTime, Status status) {
        return Booking.builder()
                .resourceId(resourceId)
                .organizationId(ORGANIZATION_ID)
                .userId(userId)
                .startTime(LocalDateTime.parse(startTime))
                .endTime(LocalDateTime.parse(endTime))
                .status(status)
                .build();
    }

    private JsonNode readJsonNode(String path) throws IOException {
        return OBJECT_MAPPER.readTree(readJson(path));
    }

    private String readJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
