package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;


@SpringBootApplication
public class WebappApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepo, EventRepository eventRepo, RegistrationRepository registrationRepo, CategoryRepository categoryRepo, KeywordRepository keywordRepo) {
		return args -> {
			// Organisers
			User csit = userRepo.save(new User("CSIT Club", "csit@uni.edu", UserType.ORGANISER, "password"));
			User nerdSociety = userRepo.save(new User("Nerd Society", "nerds@uni.edu", UserType.ORGANISER, "password"));

			// Students
			User alice = userRepo.save(new User("Alice Blogs", "alice@uni.edu", UserType.STUDENT, "password"));
			User charlie = userRepo.save(new User("Charlie Brown", "charlie@uni.edu", UserType.STUDENT, "password"));
			User diana = userRepo.save(new User("Diana Prince", "diana@uni.edu", UserType.STUDENT, "password"));
			User eric = userRepo.save(new User("Eric Yang", "eric@uni.edu", UserType.STUDENT, "password"));

			String category1 = "CAT1";
			String category2 = "CAT2";


			// Events
			Event hackathon = eventRepo.save(new Event(
					"Hackathon", "24hr coding challenge",
					LocalDateTime.now().plusDays(2), LocalDateTime.now(),
					10, "Library", csit
			));
			hackathon.addCategory(category1);

			Event bbqNight = eventRepo.save(new Event(
					"BBQ Night", "Free food and fun networking!",
					LocalDateTime.now().plusDays(5), LocalDateTime.now(),
					10, "Campus Park", csit
			));
			hackathon.addCategory(category1);

			Event musicJam = eventRepo.save(new Event(
					"Music Jam", "Bring your instruments and jam with others!",
					LocalDateTime.now().plusDays(3), LocalDateTime.now(), 5, "Auditorium", nerdSociety
			));
			hackathon.addCategory(category2);

			Event gameNight = eventRepo.save(new Event(
					"Game Night", "Board games and snacks",
					LocalDateTime.now().plusDays(7), LocalDateTime.now(), 20, "Student Lounge", nerdSociety
			));

			// Registrations (students only)
			registrationRepo.save(new Registration(alice, hackathon, LocalDateTime.now()));
			registrationRepo.save(new Registration(charlie, hackathon, LocalDateTime.now()));
			registrationRepo.save(new Registration(diana, hackathon, LocalDateTime.now()));

			registrationRepo.save(new Registration(eric, bbqNight, LocalDateTime.now()));
			registrationRepo.save(new Registration(alice, bbqNight, LocalDateTime.now()));

			registrationRepo.save(new Registration(charlie, musicJam, LocalDateTime.now()));
			registrationRepo.save(new Registration(diana, musicJam, LocalDateTime.now()));
			registrationRepo.save(new Registration(eric, musicJam, LocalDateTime.now()));

			registrationRepo.save(new Registration(alice, gameNight, LocalDateTime.now()));
			registrationRepo.save(new Registration(diana, gameNight, LocalDateTime.now()));
		};
	}


}