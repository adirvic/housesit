package br.com.rodrigues.housesit.dto;

import br.com.rodrigues.housesit.domain.model.Settlement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        UserDto fromUser,
        UserDto toUser,
        BigDecimal amount,
        LocalDate settlementDate,
        String note,
        LocalDateTime createdAt
) {
    public static SettlementResponse fromEntity(Settlement settlement) {
        if (settlement == null) return null;
        return new SettlementResponse(
                settlement.getId(),
                UserDto.fromEntity(settlement.getFromUser()),
                UserDto.fromEntity(settlement.getToUser()),
                settlement.getAmount(),
                settlement.getSettlementDate(),
                settlement.getNote(),
                settlement.getCreatedAt()
        );
    }
}
