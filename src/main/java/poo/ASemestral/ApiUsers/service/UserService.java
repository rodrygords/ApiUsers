package poo.ASemestral.ApiUsers.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poo.ASemestral.ApiUsers.controller.UserCreateDto;
import poo.ASemestral.ApiUsers.controller.UserResponseDto;
import poo.ASemestral.ApiUsers.controller.UserUpdateDto;
import poo.ASemestral.ApiUsers.entity.User;
import poo.ASemestral.ApiUsers.exception.DuplicateEmailException;
import poo.ASemestral.ApiUsers.exception.UserNotFoundException;
import poo.ASemestral.ApiUsers.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto createUser(UserCreateDto createDto) {
        String normalizedEmail = normalizeEmail(createDto.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        var entity = new User();
        entity.setUsername(createDto.username().trim());
        entity.setEmail(normalizedEmail);
        entity.setPassword(passwordEncoder.encode(createDto.password()));

        try {
            return toResponse(userRepository.save(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponseDto updateUserById(UUID userId, UserUpdateDto updateDto) {
        User user = findUser(userId);

        if (updateDto.username() != null) {
            user.setUsername(updateDto.username().trim());
        }

        if (updateDto.email() != null) {
            String normalizedEmail = normalizeEmail(updateDto.email());
            if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(normalizedEmail, userId)) {
                throw new DuplicateEmailException();
            }
            user.setEmail(normalizedEmail);
        }

        if (updateDto.password() != null) {
            user.setPassword(passwordEncoder.encode(updateDto.password()));
        }

        try {
            return toResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional
    public void deleteById(UUID userId) {
        User user = findUser(userId);
        userRepository.delete(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponseDto toResponse(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreationTimestamp(),
                user.getUpdateTimestamp()
        );
    }
}
