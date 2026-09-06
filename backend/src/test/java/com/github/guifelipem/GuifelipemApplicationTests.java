package com.github.guifelipem;

import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
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

	private User saveClient(String email, LocalDateTime createdAt) {
		return userRepository.save(User.builder()
				.name("Cliente")
				.email(email)
				.passwordHash("hash")
				.role(UserRole.CLIENT)
				.createdAt(createdAt)
				.build());
	}

	private Ticket saveTicket(User client, String title, TicketStatus status, LocalDateTime updatedAt) {
		return ticketRepository.save(Ticket.builder()
				.title(title)
				.description("Descrição")
				.status(status)
				.priority(TicketPriority.MEDIUM)
				.createdAt(updatedAt.minusDays(1))
				.updatedAt(updatedAt)
				.createdBy(client)
				.build());
	}

}
