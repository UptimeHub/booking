package uz.uptimehub.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BookingAppApplication {

    static void main(String[] args) {
        SpringApplication.run(BookingAppApplication.class, args);
    }

}
