package com.springproject.profEcomWebApp.security.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    private Set<String> roles;
    @NotBlank
    @Size(min = 6, message = "Password must contains 6 characters atleast")
    private String password;
}
