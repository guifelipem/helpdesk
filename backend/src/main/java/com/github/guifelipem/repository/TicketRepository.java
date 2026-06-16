package com.github.guifelipem.repository;

import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedBy(User user);

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (
                CAST(:search AS string) IS NULL OR
                LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            )
            """)
    Page<Ticket> findAllWithFilters(TicketStatus status, TicketPriority priority, String search, Pageable pageable);
}
