package au.edu.rmit.sept.webapp.controller;


import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.BanService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
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
    private final BanService banService;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserService userService, RegistrationService registrationService, BanService banService) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.banService = banService;
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
                                 @RequestParam BanType banType,
                                 @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     LocalDateTime banEndDate,
                                 @RequestParam String banReason,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

            // Get the current admin user performing the ban
            User adminUser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid admin user ID"));

            // Verify the current user is an admin
            if (!adminUser.isAdmin()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only admins can deactivate users.");
                return "redirect:/login";
            }

            // Ensure that if the ban type is TEMPORARY, a ban end date is provided
            if (banType == BanType.TEMPORARY && banEndDate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "If ban type is temporary, a ban end date must be provided.");
                return "redirect:/users";
            }

            if (banType == BanType.TEMPORARY && banEndDate.isBefore(LocalDateTime.now().plusHours(1L))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ban end date must be at least 1 hour in the future for temporary bans.");
                return "redirect:/users";
            }


            // Create a ban in the ban table (backend)
            Ban ban = new Ban(user, adminUser, banType, banReason);
            if (banType == BanType.TEMPORARY) {
                ban.setBanEndDate(banEndDate);
            }
            banService.banUser(ban);
            // For now, we'll just show a message
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getName() + "' has been deactivated. Backend has been updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deactivating user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    // Reactivate/ban user accounts
    @PostMapping("/{userId}/reactivate")
    public String reactivateUser(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable int userId,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
            // Check that the user is actually banned
            if (user.getBan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User is not currently banned.");
                return "redirect:/users";
            }

            // Check that temporary bans have not expired
            if (user.getBan().getBanType() == BanType.TEMPORARY &&
                user.getBan().getBanEndDate() != null &&
                user.getBan().getBanEndDate().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "User's temporary ban has already expired.");
                return "redirect:/users";
            }


            // Delete the ban on the user
            banService.removeBan(user);
            // For now, we'll just show a message
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getName() + "' has been reactivated. Backend should be updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error reactivating user: " + e.getMessage());
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

    // Admin: Show edit form for user role assignment
    @GetMapping("/{userId}/edit")
    public String showEditUserForm(@AuthenticationPrincipal CustomUserDetails currentUser,
                                   @PathVariable Long userId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        // Verify admin access
        User adminUser = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid admin ID"));
        
        if (!adminUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/events";
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        model.addAttribute("user", user);
        model.addAttribute("currentUser", adminUser);
        model.addAttribute("userTypes", UserType.values());
        
        return "admin-user-edit";
    }

    // Admin: Update user role
    @PostMapping("/{userId}/edit")
    public String updateUserRole(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @PathVariable Long userId,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Verify admin access
            User adminUser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid admin ID"));
            
            if (!adminUser.isAdmin()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
                return "redirect:/events";
            }

            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

            // Prevent changing own role
            if (user.getUserId().equals(adminUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot change your own role.");
                return "redirect:/users";
            }

            UserType newRole = UserType.valueOf(role);
            UserType oldRole = user.getRole();
            
            user.setRole(newRole);
            userService.save(user);

            redirectAttributes.addFlashAttribute("successMessage", 
                "User '" + user.getName() + "' role updated from " + oldRole + " to " + newRole + " successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid role selected: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user role: " + e.getMessage());
        }

        return "redirect:/users";
    }

}
