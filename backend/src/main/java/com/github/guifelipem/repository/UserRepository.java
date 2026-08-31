package com.github.guifelipem.repository;

import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
        AND (
            CAST(:search AS string) IS NULL OR
            LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
        )
        """)
    Page<User> findAllWithFilters(
            UserRole role,
            String search,
            Pageable pageable
    );
}
