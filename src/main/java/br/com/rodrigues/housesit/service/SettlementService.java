package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.Household;
import br.com.rodrigues.housesit.domain.model.Settlement;
import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.SettlementRepository;
import br.com.rodrigues.housesit.domain.repository.UserRepository;
import br.com.rodrigues.housesit.dto.SettlementRequest;
import br.com.rodrigues.housesit.dto.SettlementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    public SettlementService(SettlementRepository settlementRepository,
                             HouseholdRepository householdRepository,
                             UserRepository userRepository) {
        this.settlementRepository = settlementRepository;
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SettlementResponse recordSettlement(User currentUser, SettlementRequest request) {
        if (currentUser.getHousehold() == null) {
            throw new IllegalStateException("User does not belong to any household");
        }

        Household household = householdRepository.findById(currentUser.getHousehold().getId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        User fromUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sender user not found"));

        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient user not found"));

        if (toUser.getHousehold() == null || !toUser.getHousehold().getId().equals(household.getId())) {
            throw new IllegalArgumentException("Recipient user is not in your household");
        }

        Settlement settlement = new Settlement(
                household,
                fromUser,
                toUser,
                request.amount(),
                request.settlementDate() != null ? request.settlementDate() : LocalDate.now(),
                request.note()
        );

        settlement = settlementRepository.save(settlement);
        return SettlementResponse.fromEntity(settlement);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements(User currentUser, LocalDate startDate, LocalDate endDate) {
        if (currentUser.getHousehold() == null) {
            return List.of();
        }

        List<Settlement> settlements;
        if (startDate != null && endDate != null) {
            settlements = settlementRepository.findByHouseholdIdAndSettlementDateBetweenOrderBySettlementDateDescCreatedAtDesc(
                    currentUser.getHousehold().getId(), startDate, endDate);
        } else {
            settlements = settlementRepository.findByHouseholdIdOrderBySettlementDateDescCreatedAtDesc(
                    currentUser.getHousehold().getId());
        }

        return settlements.stream().map(SettlementResponse::fromEntity).toList();
    }
}
