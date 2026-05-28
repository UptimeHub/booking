package uz.uptimehub.booking.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HeaderUtils {

    @Value("${custom-header-names.user.id}")
    private String userIdHeader;

    @Value("${custom-header-names.organization.id}")
    private String organizationIdHeader;

    public UUID extractUserId(HttpServletRequest request) {
        return extractUuidHeader(request, userIdHeader);
    }

    public UUID extractOrganizationId(HttpServletRequest request) {
        return extractUuidHeader(request, organizationIdHeader);
    }

    private UUID extractUuidHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        return headerValue == null ? null : UUID.fromString(headerValue);
    }
}
