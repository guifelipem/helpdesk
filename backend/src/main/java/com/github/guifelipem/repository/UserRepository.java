package com.github.guifelipem.repository;

import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.dto.ticket.AgentActiveTicketsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

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

    @Query("""
        SELECT new com.github.guifelipem.dto.ticket.AgentActiveTicketsResponse(
            u.id,
            u.name,
            COUNT(t.id)
        )
        FROM User u
        LEFT JOIN Ticket t ON t.assignedTo = u
            AND t.status <> com.github.guifelipem.enums.TicketStatus.CLOSED
        WHERE u.role = com.github.guifelipem.enums.UserRole.AGENT
        GROUP BY u.id, u.name
        ORDER BY COUNT(t.id) DESC, u.name ASC, u.id ASC
        """)
    List<AgentActiveTicketsResponse> countActiveTicketsByAgent();
}
