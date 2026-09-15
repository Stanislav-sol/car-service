package com.mycar.car_service.service;

import com.mycar.car_service.dto.UserResponseDto;
import com.mycar.car_service.dto.UserUpdateRequestDto;
import com.mycar.car_service.model.Role;
import com.mycar.car_service.model.User;
import org.springframework.security.core.GrantedAuthority;
import com.mycar.car_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String login, String rawPassword, String email) {
        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User(login, encodedPassword, email, Role.USER);

        return userRepository.save(newUser);
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + id));
        return mapToDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(String login, UserUpdateRequestDto request) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email уже используется другим пользователем");
        }

        user.setEmail(request.getEmail());
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден с id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDto mapToDto(User user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new UserResponseDto(user.getId(), user.getLogin(), user.getEmail(), roles);
    }
}