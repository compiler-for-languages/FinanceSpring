package com.financeProject.MyProject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
// Used for creating/updating financial records
public class FinancialRecordRequestDTO {

    private Double amount;        // Transaction amount
    private String type;          // INCOME / EXPENSE
    private String category;      // e.g., Food, Rent
    private String notes;         // Optional

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recordDate;    // Format: YYYY-MM-DD
    private Long userId;          // To link or change the user
}
