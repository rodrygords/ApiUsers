package poo.ASemestral.ApiUsers.controller;

public record CreateUserDto(
        String username,
        String email,
        String password
) {
}