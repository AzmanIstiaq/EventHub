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
        u.setRole(UserType.STUDENT); // instead of PUBLIC
        userRepository.save(u);

        Event e = new Event();
        e.setTitle("Welcome Week");
        e.setEventDate(LocalDateTime.now().plusDays(3)); // use setDateTime
        eventRepository.save(e);

        mvc.perform(post("/events/" + e.getEventId() + "/register")
                        .param("userId", Integer.toString(u.getUserId())))
                .andExpect(status().is3xxRedirection());

        assertThat(registrationRepository.findByEvent_EventId(e.getEventId())).isNotEmpty();
    } }
