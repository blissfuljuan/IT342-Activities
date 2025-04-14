package Delima.com.example.OAuth2demo.Service;

import Delima.com.example.OAuth2demo.Entity.ExpenseEntity;
import Delima.com.example.OAuth2demo.Repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    // Constructor-based Dependency Injection
    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // Get all expenses
    public List<ExpenseEntity> getAllExpenses() {
        return expenseRepository.findAll();
    }

    // Get an expense by ID
    public Optional<ExpenseEntity> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    // Create a new expense
    public ExpenseEntity createExpense(ExpenseEntity expense) {
        // Ensure the entity is saved in the 'expenses' table
        return expenseRepository.save(expense);
    }

    // Update an existing expense
    public Optional<ExpenseEntity> updateExpense(Long id, ExpenseEntity newExpense) {
        return expenseRepository.findById(id).map(expense -> {
            // Update the expense details
            expense.setTripId(newExpense.getTripId());
            expense.setCategory(newExpense.getCategory());
            expense.setAmount(newExpense.getAmount());
            expense.setTimestamp(newExpense.getTimestamp());

            // Save the updated expense to the 'expenses' table
            return expenseRepository.save(expense);
        });
    }

    // Delete an expense
    public boolean deleteExpense(Long id) {
        return expenseRepository.findById(id).map(expense -> {
            expenseRepository.delete(expense); // Delete the expense from the 'expenses' table
            return true;
        }).orElse(false);
    }
}
