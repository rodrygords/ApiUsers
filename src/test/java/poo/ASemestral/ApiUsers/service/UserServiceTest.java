package poo.ASemestral.ApiUsers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import poo.ASemestral.ApiUsers.controller.UserCreateDto;
import poo.ASemestral.ApiUsers.controller.UserUpdateDto;
import poo.ASemestral.ApiUsers.entity.User;
import poo.ASemestral.ApiUsers.exception.DuplicateEmailException;
import poo.ASemestral.ApiUsers.exception.UserNotFoundException;
import poo.ASemestral.ApiUsers.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserTransformsPasswordIntoHash() {
        var bcrypt = new BCryptPasswordEncoder();
        var serviceWithBcrypt = new UserService(userRepository, bcrypt);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(UUID.randomUUID());
            return user;
        });

        serviceWithBcrypt.createUser(new UserCreateDto("User", "user@example.com", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(bcrypt.matches("password123", captor.getValue().getPassword()));
    }

    @Test
    void createUserDoesNotStoreOriginalPassword() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(new UserCreateDto("User", "user@example.com", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotEquals("password123", captor.getValue().getPassword());
    }

    @Test
    void createUserNormalizesEmail() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(new UserCreateDto("User", "  User@Example.COM  ", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("user@example.com", captor.getValue().getEmail());
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () ->
                userService.createUser(new UserCreateDto("User", "User@Example.com", "password123")));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void getUserByIdRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void updateUserUpdatesExistingFields() {
        User user = user("Old name", "old@example.com", "old-hash");
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndUserIdNot("new@example.com", user.getUserId()))
                .thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.updateUserById(
                user.getUserId(), new UserUpdateDto("New name", "New@Example.com", null));

        assertEquals("New name", response.username());
        assertEquals("new@example.com", response.email());
    }

    @Test
    void updateUserKeepsOwnEmailWithoutConflict() {
        User user = user("User", "user@example.com", "current-hash");
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndUserIdNot("user@example.com", user.getUserId()))
                .thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.updateUserById(
                user.getUserId(), new UserUpdateDto(null, " USER@EXAMPLE.COM ", null));

        assertEquals("user@example.com", response.email());
    }

    @Test
    void updateUserRejectsEmailUsedByAnotherUser() {
        User user = user("User", "user@example.com", "current-hash");
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndUserIdNot("other@example.com", user.getUserId()))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class, () ->
                userService.updateUserById(
                        user.getUserId(), new UserUpdateDto(null, "other@example.com", null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserWithoutPasswordPreservesCurrentHash() {
        User user = user("User", "user@example.com", "current-hash");
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserById(user.getUserId(), new UserUpdateDto("Updated", null, null));

        assertEquals("current-hash", user.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUserWithPasswordStoresNewHash() {
        User user = user("User", "user@example.com", "current-hash");
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserById(user.getUserId(), new UserUpdateDto(null, null, "new-password"));

        assertEquals("new-hash", user.getPassword());
    }

    @Test
    void deleteUserRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(userId));

        verify(userRepository, never()).delete(any());
    }

    private User user(String username, String email, String password) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new User(UUID.randomUUID(), username, email, password, now, now);
    }
}
