package com.github.guifelipem.exception;

import com.github.guifelipem.controller.AuthController;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnBadRequestForInvalidPublicRegistration() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(mock(AuthService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Dados inválidos")));
    }

    @Test
    void shouldReturnStandardErrorResponseForMalformedJson() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(mock(AuthService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("JSON inválido ou incompatível com o formato esperado"));
    }

    @Test
    void shouldReturnStandardErrorResponseForInvalidParameterType() throws Exception {
        MockMvc mockMvc = parameterMockMvc();

        mockMvc.perform(get("/test/parameters/status").param("value", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parâmetro inválido: value"));
    }

    @Test
    void shouldReturnStandardErrorResponseForMissingRequiredParameter() throws Exception {
        MockMvc mockMvc = parameterMockMvc();

        mockMvc.perform(get("/test/parameters/required"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parâmetro obrigatório ausente: value"));
    }

    private MockMvc parameterMockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new ParameterController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    @RequestMapping("/test/parameters")
    private static class ParameterController {

        @GetMapping("/status")
        void status(@RequestParam TicketStatus value) {
        }

        @GetMapping("/required")
        void required(@RequestParam Long value) {
        }
    }
}
