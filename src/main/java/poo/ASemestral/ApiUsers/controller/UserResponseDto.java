package poo.ASemestral.ApiUsers.controller;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String username,
        String email,
        Instant creationTimestamp,
        Instant updateTimestamp
) {
}
