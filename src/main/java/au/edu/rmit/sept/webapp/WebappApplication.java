package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

/**
 * Main application class.
 */
@SpringBootApplication
public class WebappApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepo,
						   EventRepository eventRepo,
						   RegistrationRepository registrationRepo,
						   CategoryRepository categoryRepo,
						   KeywordRepository keywordRepo) {
		return args -> {
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			// Organisers
			if (userRepo.findAll().isEmpty()) {
				User csit = userRepo.save(new User(
						"CSIT Club",
						"csit@uni.edu",
						passwordEncoder.encode("password"),
						UserType.ORGANISER
				));
				User nerdSociety = userRepo.save(new User(
						"Nerd Society",
						"nerds@uni.edu",
						passwordEncoder.encode("password"),
						UserType.ORGANISER
				));

				// Students
				User alice = userRepo.save(new User(
						"Alice Blogs",
						"alice@uni.edu",
						passwordEncoder.encode("password"),
						UserType.STUDENT
				));
				User charlie = userRepo.save(new User(
						"Charlie Brown",
						"charlie@uni.edu",
						passwordEncoder.encode("password"),
						UserType.STUDENT
				));
				User diana = userRepo.save(new User(
						"Diana Prince",
						"diana@uni.edu",
						passwordEncoder.encode("password"),
						UserType.STUDENT
				));
				User eric = userRepo.save(new User(
						"Eric Yang",
						"eric@uni.edu",
						passwordEncoder.encode("password"),
						UserType.STUDENT
				));

				User admin = userRepo.save(new User(
						"Admin User",
						"admin@uni.edu",
						passwordEncoder.encode("password"),
						UserType.ADMIN
				));

				// Continue with categories, events, registrations as before
				Category cat1 = categoryRepo.save(new Category("CAT1"));
				Category cat2 = categoryRepo.save(new Category("CAT2"));

				Event hackathon = eventRepo.save(new Event(
						"Hackathon", "24hr coding challenge",
						LocalDateTime.now().minusDays(2), "Library", csit, cat1
				));
				Event bbqNight = eventRepo.save(new Event(
						"BBQ Night", "Free food and fun networking!",
						LocalDateTime.now().plusDays(5), "Campus Park", csit, cat1
				));
				Event musicJam = eventRepo.save(new Event(
						"Music Jam", "Bring your instruments and jam with others!",
						LocalDateTime.now().plusDays(3), "Auditorium", nerdSociety, cat2
				));
				Event gameNight = eventRepo.save(new Event(
						"Game Night", "Board games and snacks",
						LocalDateTime.now().plusDays(7), "Student Lounge", nerdSociety, cat2
				));

				// Registrations
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
			}
		};
	}


}