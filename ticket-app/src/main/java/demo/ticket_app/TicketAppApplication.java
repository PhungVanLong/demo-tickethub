package demo.ticket_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketAppApplication.class, args);
	}

}
