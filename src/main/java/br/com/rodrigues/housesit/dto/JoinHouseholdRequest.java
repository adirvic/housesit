package br.com.rodrigues.housesit.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinHouseholdRequest(
        @NotBlank(message = "Invite code is required")
        String inviteCode
) {}
