package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
/// Used to perform user based operations and analytics when needed.
/// CRUD operations for users
@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final RegistrationService registrationService;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserService userService, RegistrationService registrationService) {
        this.userService = userService;
        this.registrationService = registrationService;

    }

    // View all users (for user management) with statistics
    @GetMapping
    public String viewAllUsers(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);

        User adminUser = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        // Calculate user type statistics
        int adminCount = (int) allUsers.stream()
                .filter(user -> user.getRole() == UserType.ADMIN)
                .count();
        int organiserCount = (int) allUsers.stream()
                .filter(user -> user.getRole() == UserType.ORGANISER)
                .count();
        int studentCount = (int) allUsers.stream()
                .filter(user -> user.getRole() == UserType.STUDENT)
                .count();
                
        model.addAttribute("currentUser", adminUser);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("organiserCount", organiserCount);
        model.addAttribute("studentCount", studentCount);

        return "admin-users";
    }

    // Deactivate/ban user accounts
    @PostMapping("/{userId}/deactivate")
    public String deactivateUser(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable int userId,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

            // Add a status field to User model or implement soft delete
            // For now, we'll just show a message
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getName() + "' has been deactivated. Backend has not been updated yet.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deactivating user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    // Get a single user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get events this user is attending
    @GetMapping("/{id}/attendingEvents")
    public ResponseEntity<List<Event>> getAttendingEvents(@PathVariable long id) {
        var events = registrationService.findByUserId(id)
                .stream()
                .map(Registration::getEvent)
                .collect(Collectors.toList());

        return ResponseEntity.ok(events);
    }


    // Get events this user has organized
    @GetMapping("/{id}/organizedEvents")
    public ResponseEntity<Set<Event>> getOrganizedEvents(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(user.getOrganisedEvents()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
        model.addAttribute("formAction", "/users/profile/save");
        model.addAttribute("currentUser", user);

        return "manage-profile";
    }

    @GetMapping("/profile/create")
    public String createProfile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        List<UserType> userTypes = Arrays.stream(UserType.values())
                .filter(type -> type == UserType.STUDENT || type == UserType.ORGANISER)
                .toList();
        model.addAttribute("formAction", "/users/profile/create");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userTypes", userTypes);
        return "manage-profile";
    }

    @PostMapping("/profile/create")
    public String createProfileSave(@RequestParam String name,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String role,
                                    RedirectAttributes redirectAttrs) {
        if (userService.findByEmail(email).isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Email already in use");

            return "redirect:/users/profile/create";
        }

        UserType selectedRole = UserType.valueOf(role);
        if (selectedRole == UserType.ADMIN) {
            redirectAttrs.addFlashAttribute("error","Invalid role selection");
            return "redirect:/users/profile/create";
        }

        User newUser = new User(name, email, passwordEncoder.encode(password), selectedRole);
        userService.save(newUser);
        redirectAttrs.addFlashAttribute("success", "User '" + newUser.getName() + "' has been created, login below.");
        return "redirect:/login?createSuccess";
    }


    @PostMapping("/profile/save")
    public String saveProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @RequestParam String name,
                              @RequestParam String email,
                              @RequestParam(required = false) String password,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Optional<User> userEmail = userService.findByEmail(email);
        if (userEmail.isPresent() && userEmail.get().getUserId() != currentUser.getId()) {
            redirectAttributes.addFlashAttribute("error", "Email already in use, please choose another.");

            return "redirect:/users/profile";
        }
        user.setName(name);
        user.setEmail(email);

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        User updatedUser = userService.save(user);

        // 🔑 Update Authentication in SecurityContext with new details
        CustomUserDetails updatedDetails = new CustomUserDetails(updatedUser);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(updatedDetails, updatedDetails.getPassword(), updatedDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        redirectAttributes.addFlashAttribute("success", "Updated profile successfully");
        return "redirect:/events";
    }

}
