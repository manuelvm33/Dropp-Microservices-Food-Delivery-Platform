package com.dropp.userinfo.service;

import com.dropp.userinfo.dto.UserDto;
import com.dropp.userinfo.entity.User;
import com.dropp.userinfo.exception.UserNotFoundException;
import com.dropp.userinfo.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    UserRepo userRepo;

    @InjectMocks
    UserService userService;

    private UserDto buildUserDto() {
        return new UserDto(USER_ID, "Manuel Pineda", "manuelpineda@gmail.com", "3101234567",
                "Address 1", "Compton");
    }

    private User buildUser() {
        return new User(USER_ID, "Manuel Pineda", "manuelpineda@gmail.com", "3101234567",
                "Address 1", "Compton");
    }

    @Test
    void addUser_shouldPersistAndReturnDTO() {
        // Arrange
        UserDto inputDto = buildUserDto();
        User savedEntity = buildUser();
        when(userRepo.save(any(User.class))).thenReturn(savedEntity);

        // Act
        UserDto result = userService.addUser(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(inputDto.getUserName(), result.getUserName());
        assertEquals(inputDto.getEmail(), result.getEmail());
        verify(userRepo, times(1)).save(any(User.class));
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_whenExists_shouldReturnDTO() {
        // Arrange
        User entity = buildUser();
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(entity));

        // Act
        UserDto result = userService.getUserById(USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals("Manuel Pineda", result.getUserName());
        verify(userRepo, times(1)).findById(USER_ID);
    }

    @Test
    void getUserById_whenNotExists_shouldThrowUserNotFoundException() {
        // Arrange
        when(userRepo.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(USER_ID));

        assertEquals("User not found with id: " + USER_ID, ex.getMessage());
        verify(userRepo, times(1)).findById(USER_ID);
    }
}