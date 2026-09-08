package com.github.guifelipem.service;

import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.TicketAlreadyAssignedException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class AssignToMeConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldAllowOnlyOneAgentToAssignTheSameTicket() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        User client = saveUser("Cliente", "concurrency-client@example.com", UserRole.CLIENT, now);
        User firstAgent = saveUser("Agente 1", "concurrency-agent-1@example.com", UserRole.AGENT, now);
        User secondAgent = saveUser("Agente 2", "concurrency-agent-2@example.com", UserRole.AGENT, now);
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .title("Chamado disputado")
                .description("Dois agentes tentam assumir este chamado ao mesmo tempo")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.HIGH)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(client)
                .build());

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<AssignmentAttempt> firstAttempt = executor.submit(
                    () -> assignWhenReleased(ticket.getId(), firstAgent.getEmail(), ready, start));
            Future<AssignmentAttempt> secondAttempt = executor.submit(
                    () -> assignWhenReleased(ticket.getId(), secondAgent.getEmail(), ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS), "As duas requisições deveriam estar prontas");
            start.countDown();

            List<AssignmentAttempt> attempts = List.of(
                    getResult(firstAttempt),
                    getResult(secondAttempt)
            );

            List<AssignmentAttempt> successes = attempts.stream()
                    .filter(attempt -> attempt.response() != null)
                    .toList();
            List<AssignmentAttempt> failures = attempts.stream()
                    .filter(attempt -> attempt.failure() != null)
                    .toList();

            assertEquals(1, successes.size());
            assertEquals(1, failures.size());
            assertInstanceOf(TicketAlreadyAssignedException.class, failures.getFirst().failure());

            TicketResponse winningResponse = successes.getFirst().response();
            Ticket persistedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();

            assertEquals(TicketStatus.IN_PROGRESS, persistedTicket.getStatus());
            assertEquals(winningResponse.assignedTo().id(), persistedTicket.getAssignedTo().getId());
            assertEquals(2, ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).size());
        }
    }

    private AssignmentAttempt assignWhenReleased(
            Long ticketId,
            String agentEmail,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(agentEmail, null, List.of())
        );
        ready.countDown();
        start.await();

        try {
            return new AssignmentAttempt(ticketService.assignToMe(ticketId), null);
        } catch (RuntimeException exception) {
            return new AssignmentAttempt(null, exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private AssignmentAttempt getResult(Future<AssignmentAttempt> future) throws Exception {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException exception) {
            throw new AssertionError("A tentativa concorrente falhou inesperadamente", exception.getCause());
        }
    }

    private User saveUser(String name, String email, UserRole role, LocalDateTime createdAt) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash("hash")
                .role(role)
                .createdAt(createdAt)
                .build());
    }

    private record AssignmentAttempt(TicketResponse response, RuntimeException failure) {
    }
}
