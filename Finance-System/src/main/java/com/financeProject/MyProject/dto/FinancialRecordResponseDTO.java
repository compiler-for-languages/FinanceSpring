package com.financeProject.MyProject.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
// Used when returning financial records to client
public class FinancialRecordResponseDTO {

    private Long id;
    private Double amount;
    private String type;
    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    // Optional: include user info (simple form)
    private Long userId;
    private String userName;
    private String description;
}
