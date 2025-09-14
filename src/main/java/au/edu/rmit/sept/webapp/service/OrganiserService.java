package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.OrganiserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganiserService {
    private final OrganiserRepository repo;

    public OrganiserService(OrganiserRepository repo) {
        this.repo = repo;
    }

    public User save(User organiser) {
        return repo.save(organiser);
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public List<User> findAll() {
        return repo.findAll();
    }
}
