package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventGallery;
import au.edu.rmit.sept.webapp.repository.EventGalleryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class EventGalleryService {
    private final EventGalleryRepository repository;
    private final Path uploadDir = Paths.get("src/main/resources/static/uploads");

    public EventGalleryService(EventGalleryRepository repository) throws IOException {
        this.repository = repository;
        if  (Files.notExists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public void uploadPhoto(Event event, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file!");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed!");
        }

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        EventGallery gallery = new EventGallery(event, filename);
        repository.save(gallery);
    }

    public List<EventGallery> getPhotosByEvent(Event event) {
        return repository.findByEvent(event);
    }

}
