package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.AuditLogService;
import au.edu.rmit.sept.webapp.service.BanService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final RegistrationService registrationService;
    private final BanService banService;
    private final AuditLogService auditLogService;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserService userService, RegistrationService registrationService, BanService banService, AuditLogService auditLogService) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.banService = banService;
        this.auditLogService = auditLogService;
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping
    public String viewAllUsers(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        log.info("=== viewAllUsers() called ===");

        // Expire temporary bans first
        log.info("Calling expireTemporaryBans()...");
        banService.expireTemporaryBans();
        log.info("expireTemporaryBans() completed");

        // Now fetch all users
        log.info("Fetching all users...");
        List<User> allUsers = userService.getAllUsers();
        log.info("Fetched {} users", allUsers.size());

        // Log ban status of each user
        int bannedCount = 0;
        for (User user : allUsers) {
            if (user.getBan() != null) {
                bannedCount++;
                log.info("User ID: {}, Name: {}, Has Ban: YES, Ban Type: {}, End Date: {}",
                        user.getUserId(),
                        user.getName(),
                        user.getBan().getBanType(),
                        user.getBan().getBanEndDate());
            }
        }
        log.info("Total banned users: {}", bannedCount);

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

        log.info("=== viewAllUsers() completed ===");
        return "admin-users";
    }

    // Rest of the methods remain the same...
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

            User adminUser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid admin user ID"));

            if (user.getBan() != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User is already banned.");
                return "redirect:/users";
            }

            if (!adminUser.isAdmin()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only admins can deactivate users.");
                return "redirect:/login";
            }

            if (user.getRole() == UserType.ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "Admins cannot be banned.");
                return "redirect:/users";
            }

            if (banType == BanType.TEMPORARY && banEndDate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "If ban type is temporary, a ban end date must be provided.");
                return "redirect:/users";
            }

            if (banType == BanType.TEMPORARY && banEndDate.isBefore(LocalDateTime.now().plusHours(1L))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ban end date must be at least 1 hour in the future for temporary bans.");
                return "redirect:/users";
            }

            Ban ban = new Ban(user, adminUser, banType, banReason);
            if (banType == BanType.TEMPORARY) {
                ban.setBanEndDate(banEndDate);
            }
            banService.banUser(ban);

            AuditLog auditLog = new AuditLog();
            auditLog.setAdminUserId(adminUser.getUserId());
            auditLog.setAction(AdminAction.BAN_USER);
            auditLog.setTargetType(AdminTargetType.USER);
            auditLog.setTargetId(user.getUserId());
            String details = String.format("User '%s' (ID: %d) banned by admin '%s' (ID: %d): Type: %s, Reason: %s.",
                    user.getName(), user.getUserId(), adminUser.getName(), adminUser.getUserId(), banType, banReason);
            if (banType == BanType.TEMPORARY) {
                details += String.format(" End date: %s.", banEndDate);
            }
            auditLog.setDetails(details);
            auditLogService.record(auditLog.getAdminUserId(), auditLog.getAction(), auditLog.getTargetType(), auditLog.getTargetId(), auditLog.getDetails());

            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getName() + "' has been deactivated. Backend has been updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deactivating user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    @PostMapping("/{userId}/reactivate")
    public String reactivateUser(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable int userId,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
            if (user.getBan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User is not currently banned.");
                return "redirect:/users";
            }

            if (user.getBan().getBanType() == BanType.TEMPORARY &&
                    user.getBan().getBanEndDate() != null &&
                    user.getBan().getBanEndDate().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "User's temporary ban has already expired.");
                return "redirect:/users";
            }

            banService.removeBan(user);

            AuditLog auditLog = new AuditLog();
            auditLog.setAdminUserId(currentUser.getId());
            auditLog.setAction(AdminAction.BAN_REMOVE);
            auditLog.setTargetType(AdminTargetType.USER);
            auditLog.setTargetId(user.getUserId());
            String details = String.format("Ban for user '%s' (ID: %d) removed by admin '%s' (ID: %d).",
                    user.getName(), user.getUserId(), currentUser.getUsername(), currentUser.getId());
            auditLog.setDetails(details);
            auditLogService.record(auditLog.getAdminUserId(), auditLog.getAction(), auditLog.getTargetType(), auditLog.getTargetId(), auditLog.getDetails());

            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getName() + "' has been reactivated. Backend should be updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error reactivating user: " + e.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/attendingEvents")
    public ResponseEntity<List<Event>> getAttendingEvents(@PathVariable long id) {
        var events = registrationService.findByUserId(id)
                .stream()
                .map(Registration::getEvent)
                .collect(Collectors.toList());

        return ResponseEntity.ok(events);
    }

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

        CustomUserDetails updatedDetails = new CustomUserDetails(updatedUser);
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(updatedDetails, updatedDetails.getPassword(), updatedDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        redirectAttributes.addFlashAttribute("success", "Updated profile successfully");
        return "redirect:/events";
    }

    @GetMapping("/{userId}/edit")
    public String showEditUserForm(@AuthenticationPrincipal CustomUserDetails currentUser,
                                   @PathVariable Long userId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
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

    @PostMapping("/{userId}/edit")
    public String updateUserRole(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @PathVariable Long userId,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        try {
            User adminUser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid admin ID"));

            if (!adminUser.isAdmin()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
                return "redirect:/events";
            }

            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

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

    @PostMapping("/{userId}/editban")
    public String editBanDetails(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @PathVariable int userId,
                                 @RequestParam BanType banType,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 LocalDateTime banEndDate,
                                 @RequestParam String banReason,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
            Ban existingBan = user.getBan();
            if (existingBan == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User is not currently banned.");
                return "redirect:/users";
            }

            if (existingBan.getBanType() == BanType.TEMPORARY &&
                    existingBan.getBanEndDate() != null &&
                    existingBan.getBanEndDate().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "User's temporary ban has already expired.");
                return "redirect:/users";
            }

            User adminUser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid admin user ID"));

            if (!adminUser.isAdmin()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only admins can edit bans.");
                return "redirect:/login";
            }

            if (banType == null || banReason == null || banReason.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ban type and reason are required.");
                return "redirect:/users";
            }
            if (banType == BanType.TEMPORARY && banEndDate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "If ban type is temporary, a ban end date must be provided.");
                return "redirect:/users";
            }

            if (banType == BanType.TEMPORARY && banEndDate.isBefore(LocalDateTime.now().plusHours(1L))) {
                redirectAttributes.addFlashAttribute("errorMessage", "New ban end date must be at least 1 hour in the future for temporary bans.");
                return "redirect:/users";
            }

            existingBan.setBanType(banType);
            existingBan.setBanReason(banReason);
            if (banType == BanType.TEMPORARY) {
                existingBan.setBanEndDate(banEndDate);
            } else {
                existingBan.setBanEndDate(null);
            }
            banService.updateBan(existingBan);

            AuditLog auditLog = new AuditLog();
            auditLog.setAdminUserId(adminUser.getUserId());
            auditLog.setAction(AdminAction.BAN_EDIT);
            auditLog.setTargetType(AdminTargetType.USER);
            auditLog.setTargetId(user.getUserId());
            String details = String.format("Ban for user '%s' (ID: %d) updated by admin '%s' (ID: %d): Type changed to %s, Reason updated.",
                    user.getName(), user.getUserId(), adminUser.getName(), adminUser.getUserId(), banType);
            if (banType == BanType.TEMPORARY) {
                details += String.format(" New end date: %s.", banEndDate);
            }
            auditLog.setDetails(details);
            auditLogService.record(auditLog.getAdminUserId(), auditLog.getAction(), auditLog.getTargetType(), auditLog.getTargetId(), auditLog.getDetails());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Ban details for user '" + user.getName() + "' have been updated successfully.");
            return "redirect:/users";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating ban details: " + e.getMessage());
            return "redirect:/users";
        }
    }
}