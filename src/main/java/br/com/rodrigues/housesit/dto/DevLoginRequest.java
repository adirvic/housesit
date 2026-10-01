package br.com.rodrigues.housesit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DevLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        String avatarUrl
) {}
