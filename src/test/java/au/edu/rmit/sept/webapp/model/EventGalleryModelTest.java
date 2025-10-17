package au.edu.rmit.sept.webapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventGalleryModelTest {

    @Test
    @DisplayName("EventGallery getters and setters work correctly")
    void eventGalleryGettersSetters() {
        EventGallery gallery = new EventGallery();
        Event event = new Event();
        
        gallery.setEvent(event);
        gallery.setPhoto("photo.jpg");

        assertThat(gallery.getEvent()).isEqualTo(event);
        assertThat(gallery.getPhoto()).isEqualTo("photo.jpg");
        assertThat(gallery.getPhotoId()).isEqualTo(0L); // Default value
    }

    @Test
    @DisplayName("EventGallery constructor with parameters works correctly")
    void eventGalleryConstructorWithParameters() {
        Event event = new Event();
        
        EventGallery gallery = new EventGallery(event, "test.jpg");
        
        assertThat(gallery.getEvent()).isEqualTo(event);
        assertThat(gallery.getPhoto()).isEqualTo("test.jpg");
    }
}
