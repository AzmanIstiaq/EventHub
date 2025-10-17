package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventGallery;
import au.edu.rmit.sept.webapp.repository.EventGalleryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventGalleryServiceTest {

    @Mock
    private EventGalleryRepository repository;

    @InjectMocks
    private EventGalleryService eventGalleryService;

    @Test
    @DisplayName("uploadPhoto() throws exception for empty file")
    void uploadPhotoThrowsForEmptyFile() throws IOException {
        Event event = new Event();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> eventGalleryService.uploadPhoto(event, file));
    }

    @Test
    @DisplayName("uploadPhoto() throws exception for non-image file")
    void uploadPhotoThrowsForNonImageFile() throws IOException {
        Event event = new Event();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain");

        assertThrows(IllegalArgumentException.class, () -> eventGalleryService.uploadPhoto(event, file));
    }

    @Test
    @DisplayName("uploadPhoto() successfully uploads image file")
    void uploadPhotoSuccessfullyUploadsImage() throws IOException {
        Event event = new Event();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test image data".getBytes()));

        eventGalleryService.uploadPhoto(event, file);

        verify(repository).save(any(EventGallery.class));
    }

    @Test
    @DisplayName("getPhotosByEvent() returns photos for event")
    void getPhotosByEventReturnsPhotos() {
        Event event = new Event();
        List<EventGallery> expectedPhotos = List.of(new EventGallery(), new EventGallery());
        when(repository.findByEvent(event)).thenReturn(expectedPhotos);

        List<EventGallery> result = eventGalleryService.getPhotosByEvent(event);

        assertThat(result).isEqualTo(expectedPhotos);
    }
}
