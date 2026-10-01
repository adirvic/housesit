package br.com.rodrigues.housesit.domain.repository;

import br.com.rodrigues.housesit.domain.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByHouseholdIdOrderBySettlementDateDescCreatedAtDesc(Long householdId);
    List<Settlement> findByHouseholdIdAndSettlementDateBetweenOrderBySettlementDateDescCreatedAtDesc(
            Long householdId, LocalDate startDate, LocalDate endDate);
}
