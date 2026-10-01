package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.Expense;
import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.domain.model.Household;
import br.com.rodrigues.housesit.domain.model.SplitType;
import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.domain.repository.ExpenseRepository;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.UserRepository;
import br.com.rodrigues.housesit.dto.ExpenseRequest;
import br.com.rodrigues.housesit.dto.ExpenseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          HouseholdRepository householdRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(User currentUser, ExpenseRequest request) {
        if (currentUser.getHousehold() == null) {
            throw new IllegalStateException("You must belong to a household before creating expenses.");
        }

        Household household = householdRepository.findById(currentUser.getHousehold().getId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found"));

        User payer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SplitCalculation split = calculateShares(request.amount(), request.splitType(),
                request.customPayerShare(), request.customPartnerShare());

        Expense expense = new Expense(
                household,
                payer,
                request.title(),
                request.amount(),
                request.category(),
                request.expenseDate() != null ? request.expenseDate() : LocalDate.now(),
                request.splitType() != null ? request.splitType() : SplitType.EQUAL,
                split.payerShare(),
                split.partnerShare(),
                request.notes()
        );

        expense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(User currentUser,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            ExpenseCategory category,
                                            Long payerId) {
        if (currentUser.getHousehold() == null) {
            return List.of();
        }

        List<Expense> expenses = expenseRepository.searchExpenses(
                currentUser.getHousehold().getId(),
                startDate,
                endDate,
                category,
                payerId
        );

        return expenses.stream().map(ExpenseResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(User currentUser, Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with ID: " + id));

        validateHouseholdAccess(currentUser, expense);
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(User currentUser, Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with ID: " + id));

        validateHouseholdAccess(currentUser, expense);

        SplitCalculation split = calculateShares(request.amount(), request.splitType(),
                request.customPayerShare(), request.customPartnerShare());

        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        if (request.expenseDate() != null) {
            expense.setExpenseDate(request.expenseDate());
        }
        expense.setSplitType(request.splitType() != null ? request.splitType() : SplitType.EQUAL);
        expense.setPayerShare(split.payerShare());
        expense.setPartnerShare(split.partnerShare());
        expense.setNotes(request.notes());

        expense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public void deleteExpense(User currentUser, Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with ID: " + id));

        validateHouseholdAccess(currentUser, expense);
        expenseRepository.delete(expense);
    }

    public record SplitCalculation(BigDecimal payerShare, BigDecimal partnerShare) {}

    public SplitCalculation calculateShares(BigDecimal totalAmount, SplitType splitType,
                                           BigDecimal customPayer, BigDecimal customPartner) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new SplitCalculation(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        SplitType type = splitType != null ? splitType : SplitType.EQUAL;
        BigDecimal two = new BigDecimal("2");

        switch (type) {
            case EQUAL -> {
                BigDecimal half = totalAmount.divide(two, 2, RoundingMode.HALF_UP);
                BigDecimal otherHalf = totalAmount.subtract(half);
                return new SplitCalculation(half, otherHalf);
            }
            case FULL_PARTNER_OWES -> {
                return new SplitCalculation(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), totalAmount.setScale(2, RoundingMode.HALF_UP));
            }
            case FULL_PAYER_EXPENSE -> {
                return new SplitCalculation(totalAmount.setScale(2, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
            case PERCENTAGE -> {
                // If customPayer is the payer's percentage (e.g., 60 for 60%)
                BigDecimal payerPercent = (customPayer != null && customPayer.compareTo(BigDecimal.ZERO) >= 0)
                        ? customPayer : new BigDecimal("50");
                BigDecimal payerShare = totalAmount.multiply(payerPercent)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal partnerShare = totalAmount.subtract(payerShare);
                return new SplitCalculation(payerShare, partnerShare);
            }
            case EXACT_AMOUNTS -> {
                BigDecimal payerShare = (customPayer != null) ? customPayer.setScale(2, RoundingMode.HALF_UP)
                        : totalAmount.divide(two, 2, RoundingMode.HALF_UP);
                BigDecimal partnerShare = totalAmount.subtract(payerShare);
                return new SplitCalculation(payerShare, partnerShare);
            }
            default -> {
                BigDecimal half = totalAmount.divide(two, 2, RoundingMode.HALF_UP);
                return new SplitCalculation(half, totalAmount.subtract(half));
            }
        }
    }

    private void validateHouseholdAccess(User currentUser, Expense expense) {
        if (currentUser.getHousehold() == null ||
                !currentUser.getHousehold().getId().equals(expense.getHousehold().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized access to this expense");
        }
    }
}
