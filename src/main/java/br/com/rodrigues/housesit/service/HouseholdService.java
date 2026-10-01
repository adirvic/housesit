package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.Household;
import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.UserRepository;
import br.com.rodrigues.housesit.dto.CreateHouseholdRequest;
import br.com.rodrigues.housesit.dto.HouseholdDto;
import br.com.rodrigues.housesit.dto.JoinHouseholdRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    public HouseholdService(HouseholdRepository householdRepository, UserRepository userRepository) {
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public HouseholdDto getHouseholdForUser(User user) {
        if (user.getHousehold() == null) {
            return null;
        }
        Household household = householdRepository.findById(user.getHousehold().getId())
                .orElse(user.getHousehold());
        return HouseholdDto.fromEntity(household);
    }

    @Transactional
    public HouseholdDto createHousehold(User user, CreateHouseholdRequest request) {
        Household household = new Household(request.name(), request.currency());
        household = householdRepository.save(household);

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        household.addMember(managedUser);
        managedUser.setHousehold(household);
        userRepository.save(managedUser);

        return HouseholdDto.fromEntity(household);
    }

    @Transactional
    public HouseholdDto joinHousehold(User user, JoinHouseholdRequest request) {
        Household household = householdRepository.findByInviteCode(request.inviteCode().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code: " + request.inviteCode()));

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (managedUser.getHousehold() != null && managedUser.getHousehold().getId().equals(household.getId())) {
            return HouseholdDto.fromEntity(household);
        }

        household.addMember(managedUser);
        managedUser.setHousehold(household);
        userRepository.save(managedUser);

        return HouseholdDto.fromEntity(household);
    }

    @Transactional
    public HouseholdDto regenerateInviteCode(User user) {
        if (user.getHousehold() == null) {
            throw new IllegalStateException("User does not belong to any household");
        }
        Household household = householdRepository.findById(user.getHousehold().getId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        household.setInviteCode(Household.generateInviteCode());
        household = householdRepository.save(household);
        return HouseholdDto.fromEntity(household);
    }
}
