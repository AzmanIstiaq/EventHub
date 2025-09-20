package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev") // uses in-memory H2 from application-dev.properties
@Transactional
class AcceptanceScenariosTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired EventRepository eventRepository;
    @Autowired RegistrationRepository registrationRepository;

    @Test
    @DisplayName("As a student user, I can register for an upcoming event")
    void userRegistersForEvent() throws Exception {
        User u = new User();
        u.setName("Sam");
        u.setType(UserType.STUDENT); // instead of PUBLIC
        userRepository.save(u);

        // Create an organiser (required by Event.organiser not-null)
        User organiser = new User();
        organiser.setName("Olivia Organiser");
        organiser.setType(UserType.ORGANISER);
        userRepository.save(organiser);

        Event e = new Event();
        e.setTitle("Welcome Week");
        e.setDateTime(LocalDateTime.now().plusDays(3)); // use setDateTime
        e.setLocation("Main Hall"); // Event.location is non-null
        e.setOrganiser(organiser);   // organiser is non-null
        eventRepository.save(e);

        // Public registration endpoint lives under /events/student/register/{eventId}
        mvc.perform(post("/events/student/register/" + e.getId())
                        .param("userId", u.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(registrationRepository.findByEventId(e.getId())).isNotEmpty();
    } }

