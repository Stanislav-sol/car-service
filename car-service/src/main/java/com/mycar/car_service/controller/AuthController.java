package com.mycar.car_service.controller;

import com.mycar.car_service.dto.RegisterDto;
import com.mycar.car_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLoginPage() {
        log.info("GET login, Переход на страницу входа");
        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        log.info("GET register, Отображение формы регистрации");
        model.addAttribute("registerDto", new RegisterDto("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult bindingResult,
            Model model) {
        log.info("POST register, Попытка регистрации пользователя '{}'", registerDto.login());

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registerDto.login(), registerDto.password(), registerDto.email());
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка регистрации: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }

        return "redirect:/login?success";
    }
}