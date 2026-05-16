package com.dropp.userinfo.controller;
// ─── UserControllerTest ───────────────────────────────────────────────────────

import com.dropp.userinfo.config.GlobalExceptionHandler;
import com.dropp.userinfo.dto.UserDto;
import com.dropp.userinfo.exception.UserNotFoundException;
import com.dropp.userinfo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class UserControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    private UserDto buildUserDto() {
        return new UserDto(USER_ID, "Alex Joe", "3101234567", "alex@gmail.com",
                "Address 1", "Compton");
    }

    @Test
    void addUser_shouldReturn200WithSavedDTO() throws Exception {
        // Arrange
        UserDto inputDto = buildUserDto();
        when(userService.addUser(any(UserDto.class))).thenReturn(inputDto);

        // Act & Assert
        mockMvc.perform(post("/user/addUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.userName").value("Alex Joe"))
                .andExpect(jsonPath("$.email").value("alex@gmail.com"));
    }

    @Test
    void getUserDetailsById_whenExists_shouldReturn200() throws Exception {
        // Arrange
        UserDto dto = buildUserDto();
        when(userService.getUserById(USER_ID)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.userName").value("Alex Joe"));
    }

    @Test
    void getUserDetailsById_whenNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(userService.getUserById(USER_ID))
                .thenThrow(new UserNotFoundException(USER_ID));

        // Act & Assert
        mockMvc.perform(get("/user/{userId}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: " + USER_ID));
    }
}