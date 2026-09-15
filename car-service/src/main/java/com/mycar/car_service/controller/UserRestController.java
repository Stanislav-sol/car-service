package com.mycar.car_service.controller;


import com.mycar.car_service.dto.UserResponseByTokenDto;
import com.mycar.car_service.dto.UserResponseDto;
import com.mycar.car_service.dto.UserUpdateRequestDto;
import com.mycar.car_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);
    private final UserService userService;

    public UserRestController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseByTokenDto> getMe(@AuthenticationPrincipal UserDetails userDetails){
        log.info("GET метод, запрос себя");
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserResponseByTokenDto response = new UserResponseByTokenDto(
                userDetails.getUsername(),
                roles
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("GET запрос списка всех пользователей");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("GET запрос пользователя с id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(@AuthenticationPrincipal UserDetails userDetails,
                                                    @Valid @RequestBody UserUpdateRequestDto request) {
        log.info("PUT запрос обновления профиля пользователя: {}", userDetails.getUsername());
        UserResponseDto updatedUser = userService.updateUser(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE запрос удаления пользователя с id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
