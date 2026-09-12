package com.github.guifelipem.web;

import com.github.guifelipem.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageRequestFactoryTest {

    private final PageRequestFactory factory = new PageRequestFactory();

    @Test
    void shouldCreatePageableForAllowedSort() {
        Pageable pageable = factory.create(1, 25, "updatedAt,desc", Set.of("updatedAt"));

        assertEquals(1, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("updatedAt").getDirection());
    }

    @Test
    void shouldRejectMalformedSort() {
        assertThrows(InvalidRequestException.class,
                () -> factory.create(0, 10, "updatedAt", Set.of("updatedAt")));
    }

    @Test
    void shouldRejectUnknownSortProperty() {
        assertThrows(InvalidRequestException.class,
                () -> factory.create(0, 10, "passwordHash,asc", Set.of("updatedAt")));
    }

    @Test
    void shouldRejectOversizedPage() {
        assertThrows(InvalidRequestException.class, () -> factory.createUnsorted(0, 101));
    }
}
