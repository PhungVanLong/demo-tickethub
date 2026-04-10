package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.auth.CreateStaffAccountRequest;
import demo.ticket_app.dto.auth.CreateStaffAccountResponse;
import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<User>> getInactiveUsers() {
        List<User> users = userService.getInactiveUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID userId,
            @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(userId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable UUID userId) {
        User deactivatedUser = userService.deactivateUser(userId);
        return ResponseEntity.ok(deactivatedUser);
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<User> activateUser(@PathVariable UUID userId) {
        User activatedUser = userService.activateUser(userId);
        return ResponseEntity.ok(activatedUser);
    }

    @PostMapping("/{userId}/promote-organizer")
    public ResponseEntity<User> promoteToOrganizer(@PathVariable UUID userId) {
        User promoted = userService.promoteToOrganizer(userId);
        return ResponseEntity.ok(promoted);
    }

    @PostMapping("/staff")
    public ResponseEntity<CreateStaffAccountResponse> createStaffAccount(
            @Valid @RequestBody CreateStaffAccountRequest request) {
        User organizer = securityUtils.getCurrentUser();
        CreateStaffAccountResponse created = userService.createStaffAccountByOrganizer(organizer, request);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{userId}/demote-customer")
    public ResponseEntity<User> demoteToCustomer(@PathVariable UUID userId) {
        User demoted = userService.demoteToCustomer(userId);
        return ResponseEntity.ok(demoted);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String term) {
        List<User> users = userService.searchUsers(term);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalUsersCount() {
        long count = userService.getTotalUsersCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/role/{role}")
    public ResponseEntity<Long> getUsersByRoleCount(@PathVariable UserRole role) {
        long count = userService.getUsersByRoleCount(role);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/active")
    public ResponseEntity<Long> getActiveUsersCount() {
        long count = userService.getActiveUsersCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/inactive")
    public ResponseEntity<Long> getInactiveUsersCount() {
        long count = userService.getInactiveUsersCount();
        return ResponseEntity.ok(count);
    }
}
