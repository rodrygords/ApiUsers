package poo.ASemestral.ApiUsers.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("O e-mail informado já está cadastrado");
    }
}
