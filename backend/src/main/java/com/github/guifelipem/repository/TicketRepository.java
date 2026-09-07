package com.github.guifelipem.repository;

import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.dto.ticket.TicketQueueSummaryResponse;
import com.github.guifelipem.dto.ticket.AdminTicketDashboardSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByAssignedToAndStatusNot(User assignedTo, TicketStatus status);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.createdBy.id = :clientId
            AND t.status IN :statuses
            AND (:priority IS NULL OR t.priority = :priority)
            AND (
                CAST(:search AS string) IS NULL OR
                LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            )
            """)
    Page<Ticket> findAllCreatedByWithFilters(
            Long clientId,
            Set<TicketStatus> statuses,
            TicketPriority priority,
            String search,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (:agentId IS NULL OR t.assignedTo.id = :agentId)
            AND (:activeOnly = FALSE OR t.status <> com.github.guifelipem.enums.TicketStatus.CLOSED)
            AND (:unassignedOnly = FALSE OR t.assignedTo IS NULL)
            AND (
                CAST(:search AS string) IS NULL OR
                LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            )
            """)
    Page<Ticket> findAllWithFilters(
            TicketStatus status,
            TicketPriority priority,
            Long agentId,
            boolean activeOnly,
            boolean unassignedOnly,
            String search,
            Pageable pageable
    );

    @Query("""
    SELECT t FROM Ticket t
    WHERE (t.assignedTo IS NULL OR t.assignedTo.id = :agentId)
    AND (:status IS NULL OR t.status = :status)
    AND (:priority IS NULL OR t.priority = :priority)
    AND (
        CAST(:search AS string) IS NULL OR
        LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
        LOWER(t.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
    )
    """)
    Page<Ticket> findAllVisibleToAgentWithFilters(
            Long agentId,
            TicketStatus status,
            TicketPriority priority,
            String search,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.assignedTo IS NULL
            AND t.status = com.github.guifelipem.enums.TicketStatus.OPEN
            ORDER BY
                CASE t.priority
                    WHEN com.github.guifelipem.enums.TicketPriority.HIGH THEN 0
                    WHEN com.github.guifelipem.enums.TicketPriority.MEDIUM THEN 1
                    WHEN com.github.guifelipem.enums.TicketPriority.LOW THEN 2
                END,
                t.createdAt ASC,
                t.id ASC
            """)
    Page<Ticket> findAvailableForAgent(Pageable pageable);

    Page<Ticket> findAllByAssignedToId(Long agentId, Pageable pageable);

    Page<Ticket> findAllByAssignedToIdAndStatus(Long agentId, TicketStatus status, Pageable pageable);

    @Query("""
            SELECT new com.github.guifelipem.dto.ticket.TicketQueueSummaryResponse(
                COALESCE(SUM(CASE WHEN t.assignedTo IS NULL
                    AND t.status = com.github.guifelipem.enums.TicketStatus.OPEN THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.assignedTo.id = :agentId THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.assignedTo.id = :agentId
                    AND t.status = com.github.guifelipem.enums.TicketStatus.WAITING_CLIENT THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.assignedTo.id = :agentId
                    AND t.status = com.github.guifelipem.enums.TicketStatus.WAITING_AGENT THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.assignedTo.id = :agentId
                    AND t.status = com.github.guifelipem.enums.TicketStatus.RESOLVED THEN 1 ELSE 0 END), 0)
            )
            FROM Ticket t
            """)
    TicketQueueSummaryResponse summarizeQueuesForAgent(@Param("agentId") Long agentId);

    @Query("""
            SELECT new com.github.guifelipem.dto.ticket.AdminTicketDashboardSummary(
                COALESCE(SUM(CASE WHEN t.status <> com.github.guifelipem.enums.TicketStatus.CLOSED THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.assignedTo IS NULL
                    AND t.status <> com.github.guifelipem.enums.TicketStatus.CLOSED THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.status = com.github.guifelipem.enums.TicketStatus.IN_PROGRESS THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.status = com.github.guifelipem.enums.TicketStatus.WAITING_CLIENT THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.status = com.github.guifelipem.enums.TicketStatus.WAITING_AGENT THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN t.status = com.github.guifelipem.enums.TicketStatus.RESOLVED THEN 1 ELSE 0 END), 0)
            )
            FROM Ticket t
            """)
    AdminTicketDashboardSummary summarizeForAdminDashboard();

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.status <> com.github.guifelipem.enums.TicketStatus.CLOSED
            AND (
                t.assignedTo IS NULL OR
                t.priority = com.github.guifelipem.enums.TicketPriority.HIGH OR
                t.updatedAt < :staleBefore
            )
            ORDER BY
                CASE WHEN t.assignedTo IS NULL THEN 0 ELSE 1 END,
                CASE WHEN t.priority = com.github.guifelipem.enums.TicketPriority.HIGH THEN 0 ELSE 1 END,
                t.updatedAt ASC,
                t.id ASC
            """)
    Page<Ticket> findRequiringAdminAttention(LocalDateTime staleBefore, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Ticket t
        SET t.assignedTo = :agent,
            t.status = :newStatus,
            t.updatedAt = :updatedAt
        WHERE t.id = :ticketId
          AND t.assignedTo IS NULL
          AND t.status = :expectedStatus
    """)
    int assignIfAvailable(
            @Param("ticketId") Long ticketId,
            @Param("agent") User agent,
            @Param("expectedStatus") TicketStatus expectedStatus,
            @Param("newStatus") TicketStatus newStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
