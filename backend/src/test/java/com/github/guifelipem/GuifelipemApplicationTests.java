package com.github.guifelipem;

import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.dto.ticket.TicketQueueSummaryResponse;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GuifelipemApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldFilterClientTicketsByMultipleStatusesAndOrderByLastUpdate() {
		LocalDateTime now = LocalDateTime.now();
		User client = saveClient("client-overview@example.com", now);
		User otherClient = saveClient("other-client-overview@example.com", now);

		Ticket olderWaitingClient = saveTicket(client, "Aguardando cliente", TicketStatus.WAITING_CLIENT, now.minusHours(2));
		Ticket newerResolved = saveTicket(client, "Resolvido", TicketStatus.RESOLVED, now.minusHours(1));
		saveTicket(client, "Fechado", TicketStatus.CLOSED, now);
		saveTicket(otherClient, "Chamado de outro cliente", TicketStatus.RESOLVED, now.plusHours(1));

		Page<Ticket> result = ticketRepository.findAllCreatedByWithFilters(
				client.getId(),
				Set.of(TicketStatus.WAITING_CLIENT, TicketStatus.RESOLVED),
				null,
				null,
				PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"))
		);

		assertEquals(2, result.getTotalElements());
		assertEquals(newerResolved.getId(), result.getContent().get(0).getId());
		assertEquals(olderWaitingClient.getId(), result.getContent().get(1).getId());
	}

	@Test
	void shouldReturnOnlyOpenUnassignedTicketsOrderedByPriorityThenAgeAndPaginated() {
		LocalDateTime now = LocalDateTime.now();
		User client = saveClient("available-client@example.com", now);
		User otherAgent = saveAgent("other-available-agent@example.com", now);

		Ticket oldestHigh = saveTicket(client, null, "Alta antiga", TicketStatus.OPEN,
				TicketPriority.HIGH, now.minusDays(3), now.minusDays(3));
		Ticket newestHigh = saveTicket(client, null, "Alta nova", TicketStatus.OPEN,
				TicketPriority.HIGH, now.minusDays(1), now.minusDays(1));
		saveTicket(client, null, "Média", TicketStatus.OPEN,
				TicketPriority.MEDIUM, now.minusDays(5), now.minusDays(5));
		saveTicket(client, otherAgent, "Atribuído a outro", TicketStatus.OPEN,
				TicketPriority.HIGH, now.minusDays(10), now.minusDays(10));
		saveTicket(client, null, "Status incorreto", TicketStatus.WAITING_AGENT,
				TicketPriority.HIGH, now.minusDays(10), now.minusDays(10));

		Page<Ticket> firstPage = ticketRepository.findAvailableForAgent(PageRequest.of(0, 2));
		Page<Ticket> secondPage = ticketRepository.findAvailableForAgent(PageRequest.of(1, 2));

		assertEquals(3, firstPage.getTotalElements());
		assertEquals(2, firstPage.getTotalPages());
		assertEquals(oldestHigh.getId(), firstPage.getContent().get(0).getId());
		assertEquals(newestHigh.getId(), firstPage.getContent().get(1).getId());
		assertEquals("Média", secondPage.getContent().getFirst().getTitle());
	}

	@Test
	void shouldScopePersonalQueuesAndCountersToAuthenticatedAgentId() {
		LocalDateTime now = LocalDateTime.now();
		User client = saveClient("queues-client@example.com", now);
		User agent = saveAgent("queues-agent@example.com", now);
		User otherAgent = saveAgent("queues-other-agent@example.com", now);

		Ticket waitingClientOld = saveTicket(client, agent, "Cliente antigo", TicketStatus.WAITING_CLIENT,
				TicketPriority.LOW, now.minusDays(4), now.minusHours(4));
		Ticket waitingClientNew = saveTicket(client, agent, "Cliente novo", TicketStatus.WAITING_CLIENT,
				TicketPriority.HIGH, now.minusDays(3), now.minusHours(1));
		Ticket waitingAgent = saveTicket(client, agent, "Minha resposta", TicketStatus.WAITING_AGENT,
				TicketPriority.MEDIUM, now.minusDays(2), now.minusHours(3));
		Ticket resolved = saveTicket(client, agent, "Resolvido", TicketStatus.RESOLVED,
				TicketPriority.MEDIUM, now.minusDays(1), now.minusMinutes(30));
		saveTicket(client, otherAgent, "De outro agente", TicketStatus.WAITING_AGENT,
				TicketPriority.HIGH, now.minusDays(5), now.minusDays(5));
		saveTicket(client, null, "Disponível", TicketStatus.OPEN,
				TicketPriority.LOW, now.minusDays(6), now.minusDays(6));
		saveTicket(client, null, "Livre inconsistente", TicketStatus.WAITING_CLIENT,
				TicketPriority.LOW, now.minusDays(6), now.minusDays(6));

		Page<Ticket> myTickets = ticketRepository.findAllByAssignedToId(
				agent.getId(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "updatedAt"))
		);
		Page<Ticket> waitingClientQueue = ticketRepository.findAllByAssignedToIdAndStatus(
				agent.getId(), TicketStatus.WAITING_CLIENT,
				PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "updatedAt"))
		);
		Page<Ticket> waitingAgentQueue = ticketRepository.findAllByAssignedToIdAndStatus(
				agent.getId(), TicketStatus.WAITING_AGENT, PageRequest.of(0, 10)
		);
		Page<Ticket> resolvedQueue = ticketRepository.findAllByAssignedToIdAndStatus(
				agent.getId(), TicketStatus.RESOLVED, PageRequest.of(0, 10)
		);
		TicketQueueSummaryResponse summary = ticketRepository.summarizeQueuesForAgent(agent.getId());

		assertEquals(4, myTickets.getTotalElements());
		assertTrue(myTickets.getContent().stream().allMatch(ticket -> ticket.getAssignedTo().getId().equals(agent.getId())));
		assertEquals(2, waitingClientQueue.getTotalElements());
		assertEquals(waitingClientOld.getId(), waitingClientQueue.getContent().get(0).getId());
		assertEquals(waitingClientNew.getId(), waitingClientQueue.getContent().get(1).getId());
		assertEquals(waitingAgent.getId(), waitingAgentQueue.getContent().getFirst().getId());
		assertEquals(resolved.getId(), resolvedQueue.getContent().getFirst().getId());
		assertEquals(1, summary.available());
		assertEquals(4, summary.myTickets());
		assertEquals(2, summary.waitingClient());
		assertEquals(1, summary.waitingAgent());
		assertEquals(1, summary.resolved());
	}

	private User saveClient(String email, LocalDateTime createdAt) {
		return saveUser(email, UserRole.CLIENT, createdAt);
	}

	private User saveAgent(String email, LocalDateTime createdAt) {
		return saveUser(email, UserRole.AGENT, createdAt);
	}

	private User saveUser(String email, UserRole role, LocalDateTime createdAt) {
		return userRepository.save(User.builder()
				.name("Cliente")
				.email(email)
				.passwordHash("hash")
				.role(role)
				.createdAt(createdAt)
				.build());
	}

	private Ticket saveTicket(User client, String title, TicketStatus status, LocalDateTime updatedAt) {
		return saveTicket(client, null, title, status, TicketPriority.MEDIUM,
				updatedAt.minusDays(1), updatedAt);
	}

	private Ticket saveTicket(User client, User assignedTo, String title, TicketStatus status,
							  TicketPriority priority, LocalDateTime createdAt, LocalDateTime updatedAt) {
		return ticketRepository.save(Ticket.builder()
				.title(title)
				.description("Descrição")
				.status(status)
				.priority(priority)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.createdBy(client)
				.assignedTo(assignedTo)
				.build());
	}

}
