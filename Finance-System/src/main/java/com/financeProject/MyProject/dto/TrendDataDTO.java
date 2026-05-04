package com.financeProject.MyProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendDataDTO {
    private LocalDate date;
    private Double income;
    private Double expense;
}
