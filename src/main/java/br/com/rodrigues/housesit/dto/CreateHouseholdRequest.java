package br.com.rodrigues.housesit.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHouseholdRequest(
        @NotBlank(message = "Household name is required")
        String name,
        String currency
) {}
