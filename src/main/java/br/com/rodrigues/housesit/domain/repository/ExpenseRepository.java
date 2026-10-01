package br.com.rodrigues.housesit.domain.repository;

import br.com.rodrigues.housesit.domain.model.Expense;
import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByHouseholdIdOrderByExpenseDateDescCreatedAtDesc(Long householdId);

    List<Expense> findByHouseholdIdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
            Long householdId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByHouseholdIdAndCategoryOrderByExpenseDateDesc(
            Long householdId, ExpenseCategory category);

    @Query("SELECT e FROM Expense e WHERE e.household.id = :householdId " +
           "AND (:startDate IS NULL OR e.expenseDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.expenseDate <= :endDate) " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:payerId IS NULL OR e.payer.id = :payerId) " +
           "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> searchExpenses(
            @Param("householdId") Long householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") ExpenseCategory category,
            @Param("payerId") Long payerId);
}
