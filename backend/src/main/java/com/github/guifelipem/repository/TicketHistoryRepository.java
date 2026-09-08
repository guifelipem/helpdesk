package com.github.guifelipem.repository;

import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.enums.TicketHistoryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    @Query("""
            SELECT COUNT(DISTINCT h.ticket.id)
            FROM TicketHistory h
            WHERE h.action = :action
            AND h.newValue = :newValue
            AND h.createdAt >= :from
            AND h.createdAt < :to
            """)
    long countDistinctTicketsTransitionedTo(
            @Param("action") TicketHistoryAction action,
            @Param("newValue") String newValue,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT COUNT(DISTINCT h.ticket.id)
            FROM TicketHistory h
            WHERE h.action = :action
            AND h.newValue = :newValue
            AND h.performedBy.id = :agentId
            AND h.createdAt >= :from
            AND h.createdAt < :to
            """)
    long countDistinctTicketsTransitionedToByAgent(
            @Param("agentId") Long agentId,
            @Param("action") TicketHistoryAction action,
            @Param("newValue") String newValue,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT h.ticket.createdAt, MIN(h.createdAt)
            FROM TicketHistory h
            WHERE h.action = :action
            AND h.newValue = :newValue
            AND h.performedBy.id = :agentId
            AND h.createdAt >= :from
            AND h.createdAt < :to
            GROUP BY h.ticket.id, h.ticket.createdAt
            """)
    List<Object[]> findFirstResolutionTimesByAgent(
            @Param("agentId") Long agentId,
            @Param("action") TicketHistoryAction action,
            @Param("newValue") String newValue,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT h.ticket.createdAt, MIN(h.createdAt)
            FROM TicketHistory h
            WHERE h.action = :action
            AND h.newValue = :newValue
            AND h.createdAt >= :from
            AND h.createdAt < :to
            GROUP BY h.ticket.id, h.ticket.createdAt
            """)
    List<Object[]> findFirstResolutionTimes(
            @Param("action") TicketHistoryAction action,
            @Param("newValue") String newValue,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
