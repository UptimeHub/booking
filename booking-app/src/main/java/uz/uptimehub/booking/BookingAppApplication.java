package uz.uptimehub.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients(basePackages = {
        "uz.uptimehub.booking",
        "uz.uptimehub.resource.dto.client"
})
@EnableScheduling
public class BookingAppApplication {

    static void main(String[] args) {
        SpringApplication.run(BookingAppApplication.class, args);
    }

}
