package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.Household;
import java.util.List;

public record HouseholdDto(
        Long id,
        String name,
        String inviteCode,
        String currency,
        List<UserDto> members
) {
    public static HouseholdDto fromEntity(Household household) {
        if (household == null) return null;
        List<UserDto> members = household.getMembers() != null
                ? household.getMembers().stream().map(UserDto::fromEntity).toList()
                : List.of();
        return new HouseholdDto(
                household.getId(),
                household.getName(),
                household.getInviteCode(),
                household.getCurrency(),
                members
        );
    }
}
