package poo.ASemestral.ApiUsers.service;

import org.springframework.stereotype.Service;
import poo.ASemestral.ApiUsers.controller.CreateUserDto;
import poo.ASemestral.ApiUsers.controller.UpdateUserDto;
import poo.ASemestral.ApiUsers.entity.User;
import poo.ASemestral.ApiUsers.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID createUser(CreateUserDto createUserDto) {
        // NÃO USE O CONSTRUTOR COM PARÂMETROS!
        var entity = new User();
        // NÃO SETE O UUID - deixe o @GeneratedValue fazer isso
        entity.setUsername(createUserDto.username());
        entity.setEmail(createUserDto.email());
        entity.setPassword(createUserDto.password());
        // NÃO SETE creationTimestamp nem updateTimestamp

        var userSaved = userRepository.save(entity);
        return userSaved.getUserId();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId));
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public void updateUserById(String userId, UpdateUserDto updateUserDto) {
        var id = UUID.fromString(userId);
        var userEntity = userRepository.findById(id);

        if (userEntity.isPresent()) {
            var user = userEntity.get();

            if (updateUserDto.username() != null) {
                user.setUsername(updateUserDto.username());
            }

            if (updateUserDto.email() != null) {
                user.setEmail(updateUserDto.email());
            }

            if (updateUserDto.password() != null) {
                user.setPassword(updateUserDto.password());
            }

            userRepository.save(user);
        }
    }

    public void deleteById(String userId) {
        var id = UUID.fromString(userId);
        var userExists = userRepository.existsById(id);

        if (userExists) {
            userRepository.deleteById(id);
        }
    }
}