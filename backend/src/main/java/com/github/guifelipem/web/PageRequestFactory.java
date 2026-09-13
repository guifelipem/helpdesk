package com.github.guifelipem.web;

import com.github.guifelipem.exception.InvalidRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class PageRequestFactory {

    public static final int MAX_PAGE_SIZE = 100;

    public Pageable create(int page, int size, String sort, Set<String> allowedSortProperties) {
        validatePageAndSize(page, size);
        if (sort == null || sort.isBlank()) {
            throw new InvalidRequestException("A ordenação deve usar o formato campo,direção");
        }

        String[] parts = sort.split(",", -1);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new InvalidRequestException("A ordenação deve usar o formato campo,direção");
        }

        String property = parts[0].trim();
        if (!allowedSortProperties.contains(property)) {
            throw new InvalidRequestException("Campo de ordenação não permitido: " + property);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(parts[1].trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("A direção da ordenação deve ser asc ou desc");
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    public Pageable createUnsorted(int page, int size) {
        validatePageAndSize(page, size);
        return PageRequest.of(page, size);
    }

    private void validatePageAndSize(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("A página não pode ser negativa");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("O tamanho da página deve estar entre 1 e " + MAX_PAGE_SIZE);
        }
    }
}
