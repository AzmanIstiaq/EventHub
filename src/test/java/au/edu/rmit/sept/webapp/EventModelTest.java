package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventModelTest {

    @Test
    @DisplayName("Event.getKeywordsAsString(): empty and non-empty")
    void keywordsAsString() {
        Event e = new Event();
        assertThat(e.getKeywordsAsString()).isEqualTo("");

        Keyword k1 = new Keyword(); k1.setKeyword("ai");
        Keyword k2 = new Keyword(); k2.setKeyword("ml");
        e.addKeyword(k1);
        e.addKeyword(k2);
        String s = e.getKeywordsAsString();
        assertThat(s).contains("ai").contains("ml");
    }

    @Test
    @DisplayName("Event.getStarRating(): empty=0, average computed")
    void starRating() {
        Event e = new Event();
        assertThat(e.getStarRating()).isEqualTo(0);

        Feedback f1 = new Feedback(); f1.setRating(4);
        Feedback f2 = new Feedback(); f2.setRating(2);
        e.getFeedbacks().add(f1);
        e.getFeedbacks().add(f2);
        assertThat(e.getStarRating()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("Event.checkUserRegistered(): true/false")
    void checkUserRegistered() {
        Event e = new Event();
        User u1 = new User(); u1.setUserId(1L);
        User u2 = new User(); u2.setUserId(2L);
        e.setDateTime(LocalDateTime.now());
        Registration r = new Registration(u1, e);
        e.getRegistrations().add(r);

        assertThat(e.checkUserRegistered(u1.getUserId())).isTrue();
        assertThat(e.checkUserRegistered(u2.getUserId())).isFalse();
    }
}
