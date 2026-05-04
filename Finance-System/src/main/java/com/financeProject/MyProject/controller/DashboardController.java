package com.financeProject.MyProject.controller;

import com.financeProject.MyProject.dto.CategorySummaryDTO;
import com.financeProject.MyProject.dto.DashboardSummaryDTO;
import com.financeProject.MyProject.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/*
      REST Controller for dashboard and analytics operations.

      This controller provides endpoints for financial dashboard data including:
      - Personal dashboard summaries (income, expenses, balance)
      - Company-wide analytics (for ANALYST and ADMIN roles)
      - Category-wise breakdowns
      - Monthly trends and patterns
      - Recent transaction activity

      Access Control Summary:
      - Personal endpoints (/summary, /category): Accessible by ALL authenticated users
      - Company endpoints (/company-summary, /trends, /category-analysis, /recent-activity):
        - VIEWER: Only sees their own data
        - ANALYST: Sees all users' data (read-only) + company data
        - ADMIN: Sees all users' data + full control
 */
@RestController
@RequestMapping("/dashboard") // Base path for dashboard APIs
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /*
      Retrieves role-appropriate dashboard data.
      Endpoint: GET /dashboard

      This is a unified endpoint that returns different data based on user role:
      - VIEWER: Returns only their personal dashboard data
      - ANALYST: Returns company-wide dashboard data (read-only)
      - ADMIN: Returns company-wide dashboard data (with edit capabilities)

      The service layer determines what data to return based on the user's role.
      @param principal Spring Security principal containing authenticated user's email

      @return Object containing role-appropriate dashboard data:
              - For VIEWER: Personal dashboard summary
              - For ANALYST/ADMIN: Company-wide dashboard summary

      @throws RuntimeException if user is not found
      @see DashboardService#getDashboard(String)
     */
    @GetMapping
    public ResponseEntity<?> getDashboard(Principal principal) {
        String email = principal.getName();
        Object dashboardData = dashboardService.getDashboard(email);

        return ResponseEntity.ok(dashboardData);
    }

    /*
      Retrieves the financial summary for the authenticated user.

      Endpoint: GET /dashboard/summary

      This endpoint returns key financial metrics including:
      - Total income (sum of all INCOME transactions)
      - Total expenses (sum of all EXPENSE transactions)
      - Net balance (income - expenses)

      Data is filtered to only include the currently authenticated user's
      transactions. Role-based filtering is handled in the service layer.

      @return DashboardSummaryDTO containing:
              - totalIncome: Sum of all income transactions
              - totalExpense: Sum of all expense transactions
              - netBalance: Calculated balance (income - expense)

      @throws RuntimeException if user is not found or data retrieval fails
      @see DashboardSummaryDTO
      @see DashboardService#getSummary()
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Principal principal) {
        String email = principal.getName();
        DashboardSummaryDTO summary = dashboardService.getSummary(email);

        return ResponseEntity.ok(summary);
    }


    /*
      Retrieves category-wise breakdown of transactions.
      Endpoint: GET /dashboard/category

      This endpoint returns financial data grouped by category, showing how much was spent or earned in each category.

      Use Cases:
      - Identify top spending categories (e.g., Rent, Groceries)
      - Track income sources (e.g., Salary, Freelance)
      - Generate pie charts for visualization

      @return List of CategorySummaryDTO containing for each category:
              - category: Category name (e.g., "groceries", "rent")
              - total: Total amount for this category
              - type: "INCOME" or "EXPENSE"
              - percentage: Percentage of total (calculated in service)
              - transactionCount: Number of transactions in this category
     @see CategorySummaryDTO
     @see DashboardService#getCategorySummary()
     */
    @GetMapping("/category")
    public ResponseEntity<?> getCategorySummary(Principal principal) {

        String email = principal.getName();
        List<CategorySummaryDTO> categorySummary = dashboardService.getCategorySummary(email);

        return ResponseEntity.ok(categorySummary);
    }

    /*
      Retrieves company-wide summary for ANALYST and ADMIN roles.

      Endpoint: GET /dashboard/company-summary

      This endpoint provides aggregated financial data across all users.
      Access is restricted to ANALYST and ADMIN roles only.

      Data Includes:
      - Total company income (all users combined)
      - Total company expenses (all users combined)
      - Net company balance
      - User-wise breakdown (each user's contribution)
      - Department/team aggregates (if applicable)

      Security:
      - VIEWER role receives 403 Forbidden
      - ANALYST sees read-only company data
      - ADMIN sees company data with management options
      @param principal Spring Security principal containing authenticated user's email
      @return Object containing company-wide summary data:
              - totalIncome: Sum of all users' income
              - totalExpense: Sum of all users' expenses
              - netBalance: Company net position
              - userBreakdown: Per-user totals

      @throws RuntimeException if:
              - User not found
              - User has VIEWER role (should be caught by security)
              - Data retrieval fails
      @see DashboardService#getCompanySummary(String)
     */
    @GetMapping("/company-summary")
    public ResponseEntity<?> getCompanySummary(Principal principal) {
        String email = principal.getName();
        Object companySummary = dashboardService.getCompanySummary(email);

        return ResponseEntity.ok(companySummary);
    }

    /*
      Retrieves monthly financial trends for analysis.
      Endpoint: GET /dashboard/trends

      This endpoint returns time-series data showing financial patterns over time. Useful for identifying seasonal trends and forecasting.
      Role-Based Data:
      - VIEWER: Returns their personal monthly trends
      - ANALYST/ADMIN: Returns company-wide monthly trends

      Default Behavior:
      - Returns last 6 months of data
      - Can be customized with query parameters (future enhancement)
      @param principal Spring Security principal containing authenticated user's email
      @return Object containing monthly trend data:
              - month: Year-month combination (e.g., "2026-01")
              - income: Total income for that month
              - expense: Total expenses for that month
              - net: Net balance for that month
              - growthRate: Month-over-month percentage change
      @see DashboardService#getTrends(String)
     */
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends(Principal principal) {
        Object trends = dashboardService.getTrends(principal.getName());

        return ResponseEntity.ok(trends);
    }

    /*
      Retrieves detailed category-wise financial analysis.
      Endpoint: GET /dashboard/category-analysis

      This endpoint provides comprehensive category breakdown including:
      - Income sources by category
      - Expense categories with percentages
      - Category comparisons and insights
      - Spending patterns by category

      Role-Based Data:
      - VIEWER: Returns their personal category analysis
      - ANALYST/ADMIN: Returns company-wide category analysis

      Use Cases:
      - Identify biggest spending categories
      - Track income diversification
      - Budget planning and optimization
      - Anomaly detection in spending patterns
      @param principal Spring Security principal containing authenticated user's email
      @return Object containing category analysis:
              - incomeByCategory: Income grouped by category
              - expenseByCategory: Expenses grouped by category
              - topExpenseCategories: Highest spending categories
              - topIncomeSources: Highest earning categories
              - insights: Automated insights (e.g., "Spending increased 20% in Food"
      @see DashboardService#getCategoryAnalysis(String)
     */
    @GetMapping("/category-analysis")
    public ResponseEntity<?> getCategoryAnalysis(Principal principal) {
        Object categoryAnalysis = dashboardService.getCategoryAnalysis(principal.getName());

        return ResponseEntity.ok(categoryAnalysis);
    }

    /*
      Retrieves recent transaction activity.
      Endpoint: GET /dashboard/recent-activity

      This endpoint returns the most recent transactions for quick viewing.
      Useful for dashboard widgets and real-time activity feeds.

      Role-Based Data:
      - VIEWER: Returns their own recent transactions (limited view)
      - ANALYST: Returns all users' recent transactions (read-only)
      - ADMIN: Returns all users' recent transactions (with edit options)

      Default Behavior:
      - Returns last 10 transactions
      - Sorted by date descending (newest first)
      - Can be customized with query parameters (future enhancement)

      Features:
      - Shows time ago (e.g., "2 hours ago")
      - Includes user info for ANALYST/ADMIN
      - Highlights unusual or large transactions

      @param principal Spring Security principal containing authenticated user's email

      @return Object containing recent activity:
              - transactions: List of recent transactions
              - includes for each transaction:
                - id, amount, type, category, date, description
                - timeAgo: Human-readable time (e.g., "5 minutes ago")
                - user: User info (for ANALYST/ADMIN)
                - isHighValue: Flag for transactions above threshold
      @see DashboardService#getRecentActivity(String)
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity(Principal principal) {
        Object recentActivity = dashboardService.getRecentActivity(principal.getName());

        return ResponseEntity.ok(recentActivity);
    }
}