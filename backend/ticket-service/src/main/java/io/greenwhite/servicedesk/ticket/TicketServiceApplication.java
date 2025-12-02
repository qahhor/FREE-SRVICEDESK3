package io.greenwhite.servicedesk.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Ticket Service
 */
@SpringBootApplication(scanBasePackages = {
        "io.greenwhite.servicedesk.ticket",
        "io.greenwhite.servicedesk.common"
})
@EntityScan(basePackages = {
        "io.greenwhite.servicedesk.ticket.model",
        "io.greenwhite.servicedesk.common.model"
})
@EnableJpaAuditing
@EnableScheduling
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}
