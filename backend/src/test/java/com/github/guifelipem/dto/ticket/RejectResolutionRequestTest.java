package com.github.guifelipem.dto.ticket;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RejectResolutionRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "     ", "\t", "\n"})
    void shouldRejectInvalidReason(String reason) {
        var violations = validator.validate(new RejectResolutionRequest(reason));

        assertEquals(1, violations.size());
        assertEquals("A justificativa é obrigatória", violations.iterator().next().getMessage());
    }
}
