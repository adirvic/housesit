package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.Expense;
import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.domain.model.Household;
import br.com.rodrigues.housesit.domain.model.Settlement;
import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.domain.repository.ExpenseRepository;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.SettlementRepository;
import br.com.rodrigues.housesit.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final HouseholdRepository householdRepository;

    public DashboardService(ExpenseRepository expenseRepository,
                            SettlementRepository settlementRepository,
                            HouseholdRepository householdRepository) {
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
        this.householdRepository = householdRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getMonthlySummary(User currentUser, Integer year, Integer month) {
        if (currentUser.getHousehold() == null) {
            return new DashboardSummaryResponse(
                    LocalDate.now(), LocalDate.now(), "USD",
                    BigDecimal.ZERO, List.of(), List.of(),
                    new BalanceSummaryDto(null, null, null, null, BigDecimal.ZERO, "No household created yet", true),
                    List.of()
            );
        }

        Household household = householdRepository.findById(currentUser.getHousehold().getId())
                .orElse(currentUser.getHousehold());

        LocalDate startDate;
        LocalDate endDate;

        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        } else {
            YearMonth ym = YearMonth.now();
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        }

        List<Expense> expenses = expenseRepository.findByHouseholdIdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
                household.getId(), startDate, endDate);

        List<Settlement> settlements = settlementRepository.findByHouseholdIdAndSettlementDateBetweenOrderBySettlementDateDescCreatedAtDesc(
                household.getId(), startDate, endDate);

        BigDecimal totalHouseholdSpend = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 1. Category Breakdown
        Map<ExpenseCategory, BigDecimal> categorySums = new EnumMap<>(ExpenseCategory.class);
        for (Expense e : expenses) {
            categorySums.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        List<CategorySpendDto> categoryBreakdown = categorySums.entrySet().stream()
                .map(entry -> {
                    BigDecimal catTotal = entry.getValue();
                    double percent = totalHouseholdSpend.compareTo(BigDecimal.ZERO) > 0
                            ? catTotal.multiply(new BigDecimal("100"))
                                    .divide(totalHouseholdSpend, 1, RoundingMode.HALF_UP)
                                    .doubleValue()
                            : 0.0;
                    return new CategorySpendDto(
                            entry.getKey(),
                            entry.getKey().getDisplayName(),
                            entry.getKey().getIcon(),
                            catTotal,
                            percent
                    );
                })
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .toList();

        // 2. Partner Breakdown & Balance Computation
        List<User> members = household.getMembers() != null ? household.getMembers() : List.of();
        Map<Long, BigDecimal> outOfPocketByMember = new HashMap<>();
        Map<Long, BigDecimal> expectedShareByMember = new HashMap<>();

        for (User member : members) {
            outOfPocketByMember.put(member.getId(), BigDecimal.ZERO);
            expectedShareByMember.put(member.getId(), BigDecimal.ZERO);
        }

        for (Expense e : expenses) {
            Long payerId = e.getPayer().getId();
            outOfPocketByMember.merge(payerId, e.getAmount(), BigDecimal::add);
            expectedShareByMember.merge(payerId, e.getPayerShare(), BigDecimal::add);

            // Add partner share to other member(s)
            for (User member : members) {
                if (!member.getId().equals(payerId)) {
                    expectedShareByMember.merge(member.getId(), e.getPartnerShare(), BigDecimal::add);
                }
            }
        }

        List<PartnerSpendDto> partnerBreakdown = members.stream()
                .map(member -> {
                    BigDecimal paid = outOfPocketByMember.getOrDefault(member.getId(), BigDecimal.ZERO);
                    BigDecimal share = expectedShareByMember.getOrDefault(member.getId(), BigDecimal.ZERO);
                    BigDecimal net = paid.subtract(share);
                    return new PartnerSpendDto(UserDto.fromEntity(member), paid, share, net);
                })
                .toList();

        // 3. Couple Balance Calculation (who owes who and how much)
        BalanceSummaryDto balanceSummary = calculateCoupleBalance(members, expenses, settlements);

        List<ExpenseResponse> recentExpenses = expenses.stream()
                .limit(10)
                .map(ExpenseResponse::fromEntity)
                .toList();

        return new DashboardSummaryResponse(
                startDate,
                endDate,
                household.getCurrency(),
                totalHouseholdSpend,
                categoryBreakdown,
                partnerBreakdown,
                balanceSummary,
                recentExpenses
        );
    }

    private BalanceSummaryDto calculateCoupleBalance(List<User> members, List<Expense> expenses, List<Settlement> settlements) {
        if (members.size() < 2) {
            return new BalanceSummaryDto(
                    null, null, null, null,
                    BigDecimal.ZERO,
                    members.isEmpty() ? "No members" : "Invite your partner to see shared balance & who owes who",
                    true
            );
        }

        User partner1 = members.get(0);
        User partner2 = members.get(1);

        // Calculate how much partner2 owes partner1 from expenses:
        // Partner1 paid: partner2 owes expense.partnerShare
        // Partner2 paid: partner1 owes expense.partnerShare
        BigDecimal partner2OwesPartner1 = BigDecimal.ZERO;
        BigDecimal partner1OwesPartner2 = BigDecimal.ZERO;

        for (Expense e : expenses) {
            if (e.getPayer().getId().equals(partner1.getId())) {
                partner2OwesPartner1 = partner2OwesPartner1.add(e.getPartnerShare());
            } else if (e.getPayer().getId().equals(partner2.getId())) {
                partner1OwesPartner2 = partner1OwesPartner2.add(e.getPartnerShare());
            }
        }

        // Account for direct settlements:
        // Settlement from 2 to 1 reduces partner2's debt
        // Settlement from 1 to 2 reduces partner1's debt
        for (Settlement s : settlements) {
            if (s.getFromUser().getId().equals(partner2.getId()) && s.getToUser().getId().equals(partner1.getId())) {
                partner2OwesPartner1 = partner2OwesPartner1.subtract(s.getAmount());
            } else if (s.getFromUser().getId().equals(partner1.getId()) && s.getToUser().getId().equals(partner2.getId())) {
                partner1OwesPartner2 = partner1OwesPartner2.subtract(s.getAmount());
            }
        }

        BigDecimal netOwed = partner2OwesPartner1.subtract(partner1OwesPartner2);

        if (netOwed.compareTo(BigDecimal.ZERO) > 0) {
            return new BalanceSummaryDto(
                    partner2.getId(), partner2.getName(),
                    partner1.getId(), partner1.getName(),
                    netOwed.setScale(2, RoundingMode.HALF_UP),
                    partner2.getName() + " owes " + partner1.getName() + " $" + netOwed.setScale(2, RoundingMode.HALF_UP),
                    false
            );
        } else if (netOwed.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal absOwed = netOwed.abs().setScale(2, RoundingMode.HALF_UP);
            return new BalanceSummaryDto(
                    partner1.getId(), partner1.getName(),
                    partner2.getId(), partner2.getName(),
                    absOwed,
                    partner1.getName() + " owes " + partner2.getName() + " $" + absOwed,
                    false
            );
        } else {
            return new BalanceSummaryDto(
                    null, null, null, null,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    "All settled up! No balance owed.",
                    true
            );
        }
    }
}
