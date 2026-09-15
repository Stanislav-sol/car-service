package com.mycar.car_service.dto;

import java.util.List;

public class UserResponseDto {
    private Long id;
    private String login;
    private String email;
    private List<String> roles;

    public UserResponseDto() {}

    public UserResponseDto(Long id, String login, String email, List<String> roles) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}