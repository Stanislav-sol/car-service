package com.mycar.car_service.controller;

import com.mycar.car_service.dto.AuthResponseDto;
import com.mycar.car_service.dto.LoginDto;
import com.mycar.car_service.dto.RegisterDto;
import com.mycar.car_service.model.User;
import com.mycar.car_service.service.JwtService;
import com.mycar.car_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthRestController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterDto dto) {
        User registeredUser = userService.registerUser(dto.login(), dto.password(), dto.email());
        String token = jwtService.generateToken(registeredUser);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.login(), dto.password())
        );

        User user = userService.findByLogin(dto.login())
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }
}