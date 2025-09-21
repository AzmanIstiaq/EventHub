package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}