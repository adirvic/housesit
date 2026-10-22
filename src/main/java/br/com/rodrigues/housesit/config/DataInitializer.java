package br.com.rodrigues.housesit.config;

import br.com.rodrigues.housesit.domain.model.*;
import br.com.rodrigues.housesit.domain.repository.ExpenseRepository;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.UserRepository;
import br.com.rodrigues.housesit.service.ExpenseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    public DataInitializer(UserRepository userRepository,
                           HouseholdRepository householdRepository,
                           ExpenseRepository expenseRepository,
                           ExpenseService expenseService) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Couple Household
        Household household = new Household("Our Cozy Home", "USD");
        household.setInviteCode("LOVE2026");
        household = householdRepository.save(household);

        // 2. Create Husband & Wife Users
        User husband = new User(
                "junior@example.com",
                "Junior",
                "https://api.dicebear.com/7.x/bottts/svg?seed=junior",
                "google-sub-junior"
        );
        husband.setHousehold(household);
        husband = userRepository.save(husband);

        User wife = new User(
                "ana@example.com",
                "Ana",
                "https://api.dicebear.com/7.x/bottts/svg?seed=ana",
                "google-sub-ana"
        );
        wife.setHousehold(household);
        wife = userRepository.save(wife);

        household.getMembers().add(husband);
        household.getMembers().add(wife);
        householdRepository.save(household);

        // 3. Seed initial realistic expenses
        LocalDate today = LocalDate.now();

        // Groceries paid by Junior (50/50 split)
        createSampleExpense(household, husband, "Weekly Supermarket Groceries",
                new BigDecimal("185.50"), ExpenseCategory.GROCERIES, today.minusDays(2), SplitType.EQUAL, null, null, "Trader Joe's groceries");

        // High-Speed Fiber Internet paid by Ana (50/50 split)
        createSampleExpense(household, wife, "Fiber Internet Bill",
                new BigDecimal("79.90"), ExpenseCategory.UTILITIES, today.minusDays(5), SplitType.EQUAL, null, null, "Monthly broadband");

        // Electricity Bill paid by Junior (50/50 split)
        createSampleExpense(household, husband, "Electricity Bill",
                new BigDecimal("130.00"), ExpenseCategory.UTILITIES, today.minusDays(8), SplitType.EQUAL, null, null, "Power company");

        // Date Night Dinner paid by Ana (50/50 split)
        createSampleExpense(household, wife, "Romantic Italian Dinner",
                new BigDecimal("120.00"), ExpenseCategory.DINING, today.minusDays(3), SplitType.EQUAL, null, null, "Anniversary dinner");

        // Pharmacy & Vitamins paid by Junior for Ana (100% partner owes)
        createSampleExpense(household, husband, "Prescription Medicines & Vitamins",
                new BigDecimal("45.00"), ExpenseCategory.HEALTH, today.minusDays(1), SplitType.FULL_PARTNER_OWES, null, null, "Bought vitamins for Ana");

        // Streaming Services paid by Ana (50/50 split)
        createSampleExpense(household, wife, "Netflix & Spotify Family",
                new BigDecimal("34.90"), ExpenseCategory.ENTERTAINMENT, today.minusDays(10), SplitType.EQUAL, null, null, "Monthly subscriptions");
    }

    private void createSampleExpense(Household household, User payer, String title,
                                     BigDecimal amount, ExpenseCategory category, LocalDate date,
                                     SplitType splitType, BigDecimal customPayer, BigDecimal customPartner,
                                     String notes) {
        ExpenseService.SplitCalculation split = expenseService.calculateShares(amount, splitType, customPayer, customPartner);
        Expense expense = new Expense(
                household,
                payer,
                title,
                amount,
                category,
                date,
                splitType,
                split.payerShare(),
                split.partnerShare(),
                notes
        );
        expenseRepository.save(expense);
    }
}
