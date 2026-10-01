package br.com.rodrigues.housesit.dto;

public record AuthResponse(
        String token,
        String tokenType,
        UserDto user,
        HouseholdDto household
) {
    public AuthResponse(String token, UserDto user, HouseholdDto household) {
        this(token, "Bearer", user, household);
    }
}
