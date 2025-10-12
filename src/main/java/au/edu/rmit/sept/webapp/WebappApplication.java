package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
				Category cat1 = categoryRepo.save(new Category("Music"));
				Category cat2 = categoryRepo.save(new Category("Sport"));
				Category cat3 = categoryRepo.save(new Category("Tech"));
				Category cat4 = categoryRepo.save(new Category("Food"));
				Category cat5 = categoryRepo.save(new Category("Networking"));
				Category cat6 = categoryRepo.save(new Category("Study"));
				Category cat7 = categoryRepo.save(new Category("Health"));
				Category cat8 = categoryRepo.save(new Category("Arts"));
				Category cat9 = categoryRepo.save(new Category("Gaming"));
				Category cat10 = categoryRepo.save(new Category("Outdoors"));
				Category cat11 = categoryRepo.save(new Category("Socialising"));

				Event hackathon = eventRepo.save(new Event(
						"Hackathon", "24hr coding challenge",
						LocalDateTime.now().minusDays(2),
						"RMIT University Library, Melbourne VIC",
						csit, cat3, -37.8075, 144.9630
				));

				Event bbqNight = eventRepo.save(new Event(
						"BBQ Night", "Free food and fun networking!",
						LocalDateTime.now().plusDays(5),
						"Carlton Gardens, Melbourne VIC",
						csit, cat4, -37.8057, 144.9719
				));

				Event musicJam = eventRepo.save(new Event(
						"Music Jam", "Bring your instruments and jam with others!",
						LocalDateTime.now().plusDays(3),
						"Federation Square, Melbourne VIC",
						nerdSociety, cat1, -37.8179, 144.9691
				));

				Event gameNight = eventRepo.save(new Event(
						"Game Night", "Board games and snacks",
						LocalDateTime.now().plusDays(7),
						"Melbourne Central Student Lounge, Melbourne VIC",
						nerdSociety, cat11, -37.8104, 144.9623
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