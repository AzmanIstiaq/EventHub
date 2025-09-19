package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
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
	CommandLineRunner init(UserRepository userRepo, EventRepository eventRepo, RegistrationRepository registrationRepo) {
		return args -> {
			// Organisers
			User csit = userRepo.save(new User("CSIT Club", "csit@uni.edu", "password", UserType.ORGANISER));
			User nerdSociety = userRepo.save(new User("Nerd Society", "nerds@uni.edu", "password", UserType.ORGANISER));

			// Students
			User alice = userRepo.save(new User("Alice Blogs", "alice@uni.edu", "password", UserType.STUDENT));
			User charlie = userRepo.save(new User("Charlie Brown", "charlie@uni.edu", "password", UserType.STUDENT));
			User diana = userRepo.save(new User("Diana Prince", "diana@uni.edu", "password", UserType.STUDENT));
			User eric = userRepo.save(new User("Eric Yang", "eric@uni.edu", "password", UserType.STUDENT));

			// Events
			Event hackathon = eventRepo.save(new Event(
					"Hackathon", "24hr coding challenge",
					LocalDateTime.now().plusDays(2), "Library", csit
			));

			Event bbqNight = eventRepo.save(new Event(
					"BBQ Night", "Free food and fun networking!",
					LocalDateTime.now().plusDays(5), "Campus Park", csit
			));

			Event musicJam = eventRepo.save(new Event(
					"Music Jam", "Bring your instruments and jam with others!",
					LocalDateTime.now().plusDays(3), "Auditorium", nerdSociety
			));

			Event gameNight = eventRepo.save(new Event(
					"Game Night", "Board games and snacks",
					LocalDateTime.now().plusDays(7), "Student Lounge", nerdSociety
			));

			// Registrations (students only)
			registrationRepo.save(new Registration(alice, hackathon));
			registrationRepo.save(new Registration(charlie, hackathon));
			registrationRepo.save(new Registration(diana, hackathon));

			registrationRepo.save(new Registration(eric, bbqNight));
			registrationRepo.save(new Registration(alice, bbqNight));

			registrationRepo.save(new Registration(charlie, musicJam));
			registrationRepo.save(new Registration(diana, musicJam));
			registrationRepo.save(new Registration(eric, musicJam));

			registrationRepo.save(new Registration(alice, gameNight));
			registrationRepo.save(new Registration(diana, gameNight));
		};
	}


}