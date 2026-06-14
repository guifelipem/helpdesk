package com.github.guifelipem.repository;

import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedBy(User user);
}
