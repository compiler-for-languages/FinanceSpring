package com.financeProject.MyProject.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

// Represents total amount per category
public class CategorySummaryDTO {

    private String category;
    private Double amount;

}
