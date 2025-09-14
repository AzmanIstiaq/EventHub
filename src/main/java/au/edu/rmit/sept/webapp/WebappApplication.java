package au.edu.rmit.sept.webapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.model.Event;

import java.time.LocalDateTime;


@SpringBootApplication
public class WebappApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

	@Bean
	CommandLineRunner init(EventRepository repo) {
		return args -> {
			repo.save(new Event(null, "Hackathon", "24hr coding challenge",
					LocalDateTime.now().plusDays(2), "Library", 1L));
			repo.save(new Event(null, "BBQ Night", "Free food for all!",
					LocalDateTime.now().plusDays(5), "Campus Park", 1L));
		};
	}
}