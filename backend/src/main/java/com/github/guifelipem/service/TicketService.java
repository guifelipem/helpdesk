package com.github.guifelipem.service;

import com.github.guifelipem.dto.common.PageResponse;
import com.github.guifelipem.dto.ticket.UserSummaryResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.RejectResolutionRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.TicketQueueSummaryResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.dto.ticket.TransferTicketRequest;
import com.github.guifelipem.dto.ticket.AdminTicketDashboardResponse;
import com.github.guifelipem.dto.ticket.AdminTicketDashboardSummary;
import com.github.guifelipem.dto.ticket.AdminTicketPerformanceResponse;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketHistoryAction;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.TicketQueue;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.InvalidTicketStatusTransitionException;
import com.github.guifelipem.exception.InvalidTicketManagementException;
import com.github.guifelipem.exception.TicketAlreadyAssignedException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final UserRepository userRepository;

    public TicketResponse create(CreateTicketRequest request) {

        User user = authenticatedUserProvider.getAuthenticatedUser();

        Ticket ticket = Ticket.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(user)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, TicketHistoryAction.TICKET_CREATED, null, savedTicket.getStatus().name(), user);

        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> findMyTickets(
            Set<TicketStatus> statuses,
            TicketPriority priority,
            String search,
            Pageable pageable
    ) {

        User user = authenticatedUserProvider.getAuthenticatedUser();
        String normalizedSearch = normalizeSearch(search);
        Set<TicketStatus> effectiveStatuses = statuses == null || statuses.isEmpty()
                ? EnumSet.allOf(TicketStatus.class)
                : EnumSet.copyOf(statuses);

        Page<Ticket> tickets = ticketRepository.findAllCreatedByWithFilters(
                user.getId(), effectiveStatuses, priority, normalizedSearch, pageable
        );

        return toPageResponse(tickets);
    }

    private TicketResponse toResponse(Ticket ticket) {

        UserSummaryResponse createdBy = new UserSummaryResponse(
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getName(),
                ticket.getCreatedBy().getRole()
        );

        UserSummaryResponse assignedTo = ticket.getAssignedTo() == null ? null
                : new UserSummaryResponse(
                        ticket.getAssignedTo().getId(),
                        ticket.getAssignedTo().getName(),
                        ticket.getAssignedTo().getRole()
                );

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                createdBy,
                assignedTo,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private Ticket findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Chamado não encontrado")
                );
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {

        Ticket ticket = findTicketById(id);

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() == UserRole.CLIENT && !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Você não tem permissão para visualizar este chamado");
        }

        if (user.getRole() == UserRole.AGENT && isAssignedToAnotherUser(ticket, user)) {
            throw new ForbiddenException("Somente o responsável pode visualizar este chamado");
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request) {

        Ticket ticket = findTicketById(ticketId);

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(user.getId())) {
            throw new ForbiddenException("Somente o responsável pode alterar o status deste chamado");
        }

        if (user.getRole() != null && user.getRole() != UserRole.AGENT) {
            throw new ForbiddenException("Somente agentes podem alterar o status operacional de chamados");
        }

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.status();

        if (newStatus == TicketStatus.CLOSED) {
            throw new ForbiddenException("O fechamento do chamado deve ser confirmado pelo cliente");
        }

        if (!currentStatus.canSupportTransitionTo(newStatus)) {
            throw new InvalidTicketStatusTransitionException(
                    "Transição de status inválida: " + currentStatus + " -> " + newStatus
            );
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, TicketHistoryAction.STATUS_CHANGED, currentStatus.name(), request.status().name(), user);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse closeTicket(Long ticketId) {

        Ticket ticket = findTicketById(ticketId);

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() != UserRole.CLIENT
                || !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Você não tem permissão para fechar este chamado");
        }

        if (!ticket.getStatus().canClientTransitionTo(TicketStatus.CLOSED)) {
            throw new InvalidTicketStatusTransitionException("Apenas chamados resolvidos podem ser fechados");
        }

        TicketStatus currentStatus = ticket.getStatus();

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, TicketHistoryAction.STATUS_CHANGED, currentStatus.name(), TicketStatus.CLOSED.name(), user);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse rejectResolution(Long ticketId, RejectResolutionRequest request) {

        Ticket ticket = findTicketById(ticketId);
        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() != UserRole.CLIENT
                || !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Somente o cliente dono do chamado pode rejeitar a resolução");
        }

        if (!ticket.getStatus().canClientTransitionTo(TicketStatus.IN_PROGRESS)) {
            throw new InvalidTicketStatusTransitionException(
                    "Apenas chamados resolvidos podem ter a resolução rejeitada"
            );
        }

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(
                savedTicket,
                TicketHistoryAction.RESOLUTION_REJECTED,
                TicketStatus.RESOLVED.name(),
                TicketStatus.IN_PROGRESS.name(),
                request.reason(),
                user
        );

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse sendToAgent(Long ticketId) {

        Ticket ticket = findTicketById(ticketId);
        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() != UserRole.CLIENT
                || !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Somente o cliente dono do chamado pode enviá-lo para análise do suporte");
        }

        if (ticket.getAssignedTo() == null) {
            throw new InvalidTicketStatusTransitionException(
                    "Um chamado sem responsável não pode aguardar análise do suporte"
            );
        }

        TicketStatus currentStatus = ticket.getStatus();

        if (!currentStatus.canClientTransitionTo(TicketStatus.WAITING_AGENT)) {
            throw new InvalidTicketStatusTransitionException(
                    "Apenas chamados aguardando o cliente podem ser enviados para análise do suporte"
            );
        }

        ticket.setStatus(TicketStatus.WAITING_AGENT);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(
                savedTicket,
                TicketHistoryAction.STATUS_CHANGED,
                currentStatus.name(),
                TicketStatus.WAITING_AGENT.name(),
                user
        );

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse assignToMe(Long ticketId) {
        User agent = authenticatedUserProvider.getAuthenticatedUser();

        if (agent.getRole() != null && agent.getRole() != UserRole.AGENT) {
            throw new ForbiddenException("Somente agentes podem assumir chamados");
        }

        LocalDateTime assignmentTime = LocalDateTime.now();

        int affectedRows = ticketRepository.assignIfAvailable(ticketId, agent, TicketStatus.OPEN, TicketStatus.IN_PROGRESS, assignmentTime);

        if (affectedRows == 0 ) {
            Ticket ticket = findTicketById(ticketId);

            if (ticket.getAssignedTo() != null ) {
                throw new TicketAlreadyAssignedException("Chamado já está atribuído a um agente");
            }

            throw new InvalidTicketStatusTransitionException("Apenas chamados em aberto podem ser atribuídos");
        }

        Ticket ticketUpdated = findTicketById(ticketId);

        createHistory(ticketUpdated, TicketHistoryAction.STATUS_CHANGED, TicketStatus.OPEN.name(), TicketStatus.IN_PROGRESS.name(), agent);
        createHistory(ticketUpdated, TicketHistoryAction.TICKET_ASSIGNED, null, agent.getName(), agent);

        return toResponse(ticketUpdated);
    }

    @Transactional
    public TicketResponse returnToQueue(Long ticketId) {
        Ticket ticket = findTicketById(ticketId);
        User admin = requireAdmin();

        if (ticket.getAssignedTo() == null) {
            throw new InvalidTicketManagementException("O chamado já está na fila e não possui agente responsável");
        }

        if (!canBeAdministrativelyReassigned(ticket.getStatus())) {
            throw new InvalidTicketManagementException(
                    "Chamados com status " + ticket.getStatus() + " não podem ser devolvidos para a fila"
            );
        }

        User previousAgent = ticket.getAssignedTo();
        TicketStatus previousStatus = ticket.getStatus();
        ticket.setAssignedTo(null);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        if (previousStatus != TicketStatus.OPEN) {
            createHistory(savedTicket, TicketHistoryAction.STATUS_CHANGED,
                    previousStatus.name(), TicketStatus.OPEN.name(), admin);
        }
        createHistory(savedTicket, TicketHistoryAction.TICKET_RETURNED_TO_QUEUE,
                previousAgent.getName(), "FILA",
                "Agente removido: " + previousAgent.getName() + " (ID " + previousAgent.getId() + ")", admin);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse transfer(Long ticketId, TransferTicketRequest request) {
        Ticket ticket = findTicketById(ticketId);
        User admin = requireAdmin();

        if (ticket.getAssignedTo() == null) {
            throw new InvalidTicketManagementException("Apenas chamados atribuídos podem ser transferidos");
        }

        if (!canBeAdministrativelyReassigned(ticket.getStatus())) {
            throw new InvalidTicketManagementException(
                    "Chamados com status " + ticket.getStatus() + " não podem ser transferidos"
            );
        }

        User targetAgent = userRepository.findById(request.agentId())
                .orElseThrow(() -> new UserNotFoundException("Agente de destino não encontrado"));

        if (targetAgent.getRole() != UserRole.AGENT) {
            throw new InvalidTicketManagementException("O usuário de destino precisa ter a role AGENT");
        }

        if (!targetAgent.isActive()) {
            throw new InvalidTicketManagementException("O agente de destino está bloqueado");
        }

        User previousAgent = ticket.getAssignedTo();
        if (previousAgent.getId().equals(targetAgent.getId())) {
            throw new InvalidTicketManagementException("O agente de destino já é o responsável pelo chamado");
        }

        TicketStatus previousStatus = ticket.getStatus();
        ticket.setAssignedTo(targetAgent);
        if (previousStatus == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        if (previousStatus != savedTicket.getStatus()) {
            createHistory(savedTicket, TicketHistoryAction.STATUS_CHANGED,
                    previousStatus.name(), savedTicket.getStatus().name(), admin);
        }
        createHistory(savedTicket, TicketHistoryAction.TICKET_TRANSFERRED,
                previousAgent.getName(), targetAgent.getName(),
                "Transferência de " + previousAgent.getName() + " (ID " + previousAgent.getId()
                        + ") para " + targetAgent.getName() + " (ID " + targetAgent.getId() + ")", admin);

        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> findAll(
            TicketStatus status,
            TicketPriority priority,
            Long agentId,
            Boolean active,
            Boolean unassigned,
            String search,
            Pageable pageable
    ) {

        User currentUser = authenticatedUserProvider.getAuthenticatedUser();

        String normalizedSearch = normalizeSearch(search);

        Page<Ticket> tickets;

        if (currentUser.getRole() == UserRole.AGENT) {
            tickets = ticketRepository.findAllVisibleToAgentWithFilters(
                    currentUser.getId(),
                    status,
                    priority,
                    normalizedSearch,
                    pageable
            );
        } else {
            tickets = ticketRepository.findAllWithFilters(
                    status,
                    priority,
                    agentId,
                    Boolean.TRUE.equals(active),
                    Boolean.TRUE.equals(unassigned),
                    normalizedSearch,
                    pageable
            );
        }

        return toPageResponse(tickets);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> findQueue(TicketQueue queue, Pageable pageable) {
        User agent = requireAgent();

        Page<Ticket> tickets = switch (queue) {
            case AVAILABLE -> ticketRepository.findAvailableForAgent(withoutSort(pageable));
            case MY_TICKETS -> ticketRepository.findAllByAssignedToId(
                    agent.getId(), withSort(pageable, Sort.Direction.DESC, "updatedAt")
            );
            case WAITING_CLIENT -> ticketRepository.findAllByAssignedToIdAndStatus(
                    agent.getId(), TicketStatus.WAITING_CLIENT,
                    withSort(pageable, Sort.Direction.ASC, "updatedAt")
            );
            case WAITING_AGENT -> ticketRepository.findAllByAssignedToIdAndStatus(
                    agent.getId(), TicketStatus.WAITING_AGENT,
                    withSort(pageable, Sort.Direction.ASC, "updatedAt")
            );
            case RESOLVED -> ticketRepository.findAllByAssignedToIdAndStatus(
                    agent.getId(), TicketStatus.RESOLVED,
                    withSort(pageable, Sort.Direction.DESC, "updatedAt")
            );
        };

        return toPageResponse(tickets);
    }

    @Transactional(readOnly = true)
    public TicketQueueSummaryResponse summarizeQueues() {
        User agent = requireAgent();
        return ticketRepository.summarizeQueuesForAgent(agent.getId());
    }

    @Transactional(readOnly = true)
    public AdminTicketDashboardResponse summarizeAdminDashboard() {
        AdminTicketDashboardSummary summary = ticketRepository.summarizeForAdminDashboard();

        return new AdminTicketDashboardResponse(
                summary.totalActive(),
                summary.unassigned(),
                summary.inProgress(),
                summary.waitingClient(),
                summary.waitingAgent(),
                summary.resolved(),
                userRepository.countActiveTicketsByAgent(),
                ticketRepository.findRequiringAdminAttention(
                        LocalDateTime.now().minusHours(72),
                        PageRequest.of(0, 5)
                ).getContent().stream().map(this::toResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public AdminTicketPerformanceResponse summarizeAdminPerformance(LocalDateTime from, LocalDateTime to) {
        if (!from.isBefore(to)) {
            throw new InvalidTicketManagementException("O início do período deve ser anterior ao fim");
        }

        long created = ticketRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);
        long resolved = ticketHistoryRepository.countDistinctTicketsTransitionedTo(
                TicketHistoryAction.STATUS_CHANGED, TicketStatus.RESOLVED.name(), from, to
        );
        long closed = ticketHistoryRepository.countDistinctTicketsTransitionedTo(
                TicketHistoryAction.STATUS_CHANGED, TicketStatus.CLOSED.name(), from, to
        );
        List<Object[]> resolutionTimes = ticketHistoryRepository.findFirstResolutionTimes(
                TicketHistoryAction.STATUS_CHANGED, TicketStatus.RESOLVED.name(), from, to
        );
        Long averageResolutionMinutes = resolutionTimes.isEmpty() ? null : Math.round(
                resolutionTimes.stream()
                        .mapToLong(row -> Duration.between(
                                (LocalDateTime) row[0], (LocalDateTime) row[1]
                        ).toMinutes())
                        .average()
                        .orElse(0)
        );

        return new AdminTicketPerformanceResponse(
                from, to, created, resolved, closed, averageResolutionMinutes
        );
    }

    private Pageable withoutSort(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }

    private Pageable withSort(Pageable pageable, Sort.Direction direction, String property) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(direction, property).and(Sort.by(direction, "id"))
        );
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }

    private PageResponse<TicketResponse> toPageResponse(Page<Ticket> tickets) {
        return new PageResponse<>(
                tickets.getContent().stream().map(this::toResponse).toList(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isLast()
        );
    }

    private boolean isAssignedToAnotherUser(Ticket ticket, User user) {
        return ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(user.getId());
    }

    private User requireAdmin() {
        User user = authenticatedUserProvider.getAuthenticatedUser();
        if (user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Somente administradores podem gerenciar a atribuição de chamados");
        }
        return user;
    }

    private User requireAgent() {
        User user = authenticatedUserProvider.getAuthenticatedUser();
        if (user.getRole() != UserRole.AGENT) {
            throw new ForbiddenException("Somente agentes podem acessar as filas operacionais");
        }
        return user;
    }

    private boolean canBeAdministrativelyReassigned(TicketStatus status) {
        return status == TicketStatus.OPEN
                || status == TicketStatus.IN_PROGRESS
                || status == TicketStatus.WAITING_CLIENT
                || status == TicketStatus.WAITING_AGENT;
    }

    private void createHistory(Ticket ticket, TicketHistoryAction action, String oldValue, String newValue, User performedBy) {

        createHistory(ticket, action, oldValue, newValue, null, performedBy);
    }

    private void createHistory(Ticket ticket, TicketHistoryAction action, String oldValue, String newValue,
                               String details, User performedBy) {

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .performedBy(performedBy)
                .createdAt(LocalDateTime.now())
                .build();

        ticketHistoryRepository.save(history);
    }
}
