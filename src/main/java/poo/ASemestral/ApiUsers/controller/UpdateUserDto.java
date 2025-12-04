package poo.ASemestral.ApiUsers.controller;

public record UpdateUserDto(
        String username,
        String email,
        String password
) {
}