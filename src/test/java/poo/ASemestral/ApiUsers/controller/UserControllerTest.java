package poo.ASemestral.ApiUsers.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import poo.ASemestral.ApiUsers.exception.DuplicateEmailException;
import poo.ASemestral.ApiUsers.exception.GlobalExceptionHandler;
import poo.ASemestral.ApiUsers.exception.UserNotFoundException;
import poo.ASemestral.ApiUsers.service.UserService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postReturnsCreatedWithoutPassword() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.createUser(any())).thenReturn(response(userId));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void postReturnsCorrectLocation() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.createUser(any())).thenReturn(response(userId));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/users/" + userId));
    }

    @Test
    void invalidInputReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"invalid\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void invalidUuidReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O UUID informado é inválido"));
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        when(userService.createUser(any())).thenThrow(new DuplicateEmailException());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isConflict());
    }

    @Test
    void putWithUserIdReturnsUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.updateUserById(any(), any())).thenReturn(response(userId));

        mockMvc.perform(put("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Updated user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void deleteMissingUserReturnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new UserNotFoundException(userId))
                .when(userService).deleteById(userId);

        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    private UserResponseDto response(UUID userId) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new UserResponseDto(userId, "User", "user@example.com", now, now);
    }

    private String validCreateBody() {
        return "{\"username\":\"User\",\"email\":\"user@example.com\",\"password\":\"password123\"}";
    }
}
