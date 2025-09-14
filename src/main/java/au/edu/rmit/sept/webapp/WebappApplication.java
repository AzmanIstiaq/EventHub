package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.OrganiserRepository;
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
	CommandLineRunner init(OrganiserRepository organiserRepo, EventRepository eventRepo) {
		return args -> {
			User alice = organiserRepo.save(new User("Alice Club", "alice@uni.edu", UserType.ORGANISER));
			User bob = organiserRepo.save(new User("Bob Society", "bob@uni.edu", UserType.ORGANISER));

			eventRepo.save(new Event(null, "Hackathon", "24hr coding challenge",
					LocalDateTime.now().plusDays(2), "Library", alice));
			eventRepo.save(new Event(null, "BBQ Night", "Free food!",
					LocalDateTime.now().plusDays(5), "Campus Park", alice));
			eventRepo.save(new Event(null, "Music Jam", "Bring your instruments!",
					LocalDateTime.now().plusDays(3), "Auditorium", bob));
		};
	}

}