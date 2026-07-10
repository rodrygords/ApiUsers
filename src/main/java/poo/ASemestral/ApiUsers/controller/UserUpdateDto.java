package poo.ASemestral.ApiUsers.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "O nome não pode estar em branco")
        String username,

        @Email(message = "O e-mail deve ser válido")
        @Size(min = 3, max = 254, message = "O e-mail deve ter entre 3 e 254 caracteres")
        String email,

        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String password
) {
}
