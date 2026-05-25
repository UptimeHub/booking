package uz.uptimehub.booking.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HeaderUtils {

    @Value("${custom-header-names.user.id}")
    private String userIdHeader;

    public UUID extractUserId(HttpServletRequest request) {
        return UUID.fromString(request.getHeader(userIdHeader));
    }
}