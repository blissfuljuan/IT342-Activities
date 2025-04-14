package Delima.com.example.OAuth2demo.UserController;

import Delima.com.example.OAuth2demo.Entity.ExpenseEntity;
import Delima.com.example.OAuth2demo.Service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    // Constructor-based dependency injection
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Get all expenses
    @GetMapping
    public List<ExpenseEntity> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    // Get an expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseEntity> getExpenseById(@PathVariable Long id) {
        Optional<ExpenseEntity> expense = expenseService.getExpenseById(id);
        return expense.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new expense
    @PostMapping
    public ResponseEntity<ExpenseEntity> createExpense(@Validated @RequestBody ExpenseEntity expense) {
        // Ensure all required fields are provided and valid
        ExpenseEntity createdExpense = expenseService.createExpense(expense);
        return ResponseEntity.status(201).body(createdExpense);
    }

    // Update an existing expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseEntity> updateExpense(@PathVariable Long id, @Validated @RequestBody ExpenseEntity newExpense) {
        Optional<ExpenseEntity> updatedExpense = expenseService.updateExpense(id, newExpense);
        return updatedExpense.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete an expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        boolean deleted = expenseService.deleteExpense(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
