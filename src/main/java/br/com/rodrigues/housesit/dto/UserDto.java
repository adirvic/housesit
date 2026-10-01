package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.User;

public record UserDto(
        Long id,
        String email,
        String name,
        String avatarUrl
) {
    public static UserDto fromEntity(User user) {
        if (user == null) return null;
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl()
        );
    }
}
