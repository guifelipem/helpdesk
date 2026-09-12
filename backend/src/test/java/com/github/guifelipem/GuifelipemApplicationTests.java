package com.github.guifelipem;

import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.dto.ticket.TicketQueueSummaryResponse;
import com.github.guifelipem.dto.ticket.AdminTicketDashboardSummary;
import com.github.guifelipem.dto.ticket.AgentActiveTicketsResponse;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
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

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateDemoAdministratorWithBcryptPasswordAndAllowLogin() throws Exception {
		User admin = userRepository.findByEmail("admin@helpdesk.local").orElseThrow();

		assertEquals("Administrador", admin.getName());
		assertEquals(UserRole.ADMIN, admin.getRole());
		assertTrue(admin.isActive());
		assertTrue(admin.getPasswordHash().startsWith("$2"));
		assertTrue(passwordEncoder.matches("admin@123", admin.getPasswordHash()));

		mockMvc.perform(post("/api/auth/login")
					.contentType("application/json")
					.content("{\"email\":\"admin@helpdesk.local\",\"password\":\"admin@123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void shouldReturnBadRequestWithoutSessionForInvalidPublicRegistration() throws Exception {
		mockMvc.perform(post("/api/auth/register")
					.contentType("application/json")
					.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(header().doesNotExist("Set-Cookie"));
	}

	@Test
	void shouldForbidClientFromAgentEndpoint() throws Exception {
		User client = saveClient("authorization-client@example.com", LocalDateTime.now());

		mockMvc.perform(get("/api/tickets/queues/summary")
					.header("Authorization", bearerToken(client)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void shouldForbidAgentFromAdminEndpoint() throws Exception {
		User agent = saveAgent("authorization-agent@example.com", LocalDateTime.now());

		mockMvc.perform(get("/api/users")
					.header("Authorization", bearerToken(agent)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void shouldRejectProtectedRequestWithoutToken() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void shouldForbidAdminFromAgentOperationalAction() throws Exception {
		User admin = userRepository.findByEmail("admin@helpdesk.local").orElseThrow();

		mockMvc.perform(patch("/api/tickets/999/assign/me")
					.header("Authorization", bearerToken(admin)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void shouldReturnCurrentUserForAuthenticatedRequest() throws Exception {
		User admin = userRepository.findByEmail("admin@helpdesk.local").orElseThrow();

		mockMvc.perform(get("/api/auth/me")
					.header("Authorization", bearerToken(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("admin@helpdesk.local"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
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

	@Test
	void shouldSummarizeAdminDashboardAndCountResolvedTicketsAsActive() {
		LocalDateTime now = LocalDateTime.now();
		User client = saveClient("admin-dashboard-client@example.com", now);
		User firstAgent = saveAgent("admin-dashboard-agent-1@example.com", now);
		User secondAgent = saveAgent("admin-dashboard-agent-2@example.com", now);
		User agentWithoutTickets = saveAgent("admin-dashboard-agent-3@example.com", now);

		saveTicket(client, null, "Sem responsável", TicketStatus.OPEN,
				TicketPriority.HIGH, now, now);
		saveTicket(client, firstAgent, "Em andamento", TicketStatus.IN_PROGRESS,
				TicketPriority.HIGH, now, now);
		saveTicket(client, firstAgent, "Aguardando cliente", TicketStatus.WAITING_CLIENT,
				TicketPriority.MEDIUM, now, now);
		saveTicket(client, secondAgent, "Aguardando agente", TicketStatus.WAITING_AGENT,
				TicketPriority.MEDIUM, now, now);
		saveTicket(client, secondAgent, "Resolvido ativo", TicketStatus.RESOLVED,
				TicketPriority.LOW, now, now);
		saveTicket(client, firstAgent, "Fechado", TicketStatus.CLOSED,
				TicketPriority.LOW, now, now);

		AdminTicketDashboardSummary summary = ticketRepository.summarizeForAdminDashboard();
		List<AgentActiveTicketsResponse> workloads = userRepository.countActiveTicketsByAgent();
		Page<Ticket> firstAgentActiveTickets = ticketRepository.findAllWithFilters(
				null, null, firstAgent.getId(), true, false, null, PageRequest.of(0, 10)
		);
		Page<Ticket> unassignedActiveTickets = ticketRepository.findAllWithFilters(
				null, null, null, true, true, null, PageRequest.of(0, 10)
		);

		assertEquals(5, summary.totalActive());
		assertEquals(1, summary.unassigned());
		assertEquals(1, summary.inProgress());
		assertEquals(1, summary.waitingClient());
		assertEquals(1, summary.waitingAgent());
		assertEquals(1, summary.resolved());
		assertEquals(2, activeTicketsFor(workloads, firstAgent));
		assertEquals(2, activeTicketsFor(workloads, secondAgent));
		assertEquals(0, activeTicketsFor(workloads, agentWithoutTickets));
		assertEquals(2, firstAgentActiveTickets.getTotalElements());
		assertTrue(firstAgentActiveTickets.stream().allMatch(
				ticket -> ticket.getAssignedTo().getId().equals(firstAgent.getId())
		));
		assertEquals(1, unassignedActiveTickets.getTotalElements());
		assertTrue(unassignedActiveTickets.getContent().getFirst().getAssignedTo() == null);
	}

	private long activeTicketsFor(List<AgentActiveTicketsResponse> workloads, User agent) {
		return workloads.stream()
				.filter(workload -> workload.agentId().equals(agent.getId()))
				.findFirst()
				.orElseThrow()
				.activeTickets();
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

	private String bearerToken(User user) {
		return "Bearer " + jwtService.generateToken(user.getEmail());
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
