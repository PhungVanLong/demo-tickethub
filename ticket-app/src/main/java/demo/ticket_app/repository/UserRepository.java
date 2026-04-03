package demo.ticket_app.repository;

import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByIsActive(boolean isActive);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive")
    long countByIsActive(@Param("isActive") boolean isActive);
    
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:name% OR u.email LIKE %:name%")
    List<User> findByNameOrEmailContaining(@Param("name") String name);
}
