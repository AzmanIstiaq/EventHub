package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganiserService {
    private final UserRepository repo;

    public OrganiserService(UserRepository repo) {
        this.repo = repo;
    }

    public User save(User organiser) {
        return repo.save(organiser);
    }

    public List<User> findAll() {
        return repo.findAll();
    }
}
