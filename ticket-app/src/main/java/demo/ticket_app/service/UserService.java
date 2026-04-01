package demo.ticket_app.service;

import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    public List<User> getInactiveUsers() {
        return userRepository.findByIsActive(false);
    }

    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        log.info("Created new user with id: {}", savedUser.getId());
        return savedUser;
    }

    public User updateUser(UUID userId, User userDetails) {
        User existingUser = getUserById(userId);
        
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setRole(userDetails.getRole());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("Updated user with id: {}", updatedUser.getId());
        return updatedUser;
    }

    public User deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        User deactivatedUser = userRepository.save(user);
        log.info("Deactivated user with id: {}", userId);
        return deactivatedUser;
    }

    public User activateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        User activatedUser = userRepository.save(user);
        log.info("Activated user with id: {}", userId);
        return activatedUser;
    }

    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("Deleted user with id: {}", userId);
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByNameOrEmailContaining(searchTerm);
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public long getUsersByRoleCount(UserRole role) {
        return userRepository.countByRole(role);
    }

    public long getActiveUsersCount() {
        return userRepository.countByIsActive(true);
    }

    public long getInactiveUsersCount() {
        return userRepository.countByIsActive(false);
    }

    public User promoteToOrganizer(UUID userId) {
        User user = getUserById(userId);
        if (user.getRole() == UserRole.ORGANIZER) {
            throw new IllegalArgumentException("User is already an organizer");
        }
        user.setRole(UserRole.ORGANIZER);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        log.info("Promoted user {} to ORGANIZER", userId);
        return updated;
    }

    public User demoteToCustomer(UUID userId) {
        User user = getUserById(userId);
        if (user.getRole() == UserRole.CUSTOMER) {
            throw new IllegalArgumentException("User is already a customer");
        }
        user.setRole(UserRole.CUSTOMER);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        log.info("Demoted user {} to CUSTOMER", userId);
        return updated;
    }
}
