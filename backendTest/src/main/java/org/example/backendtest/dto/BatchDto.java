package org.example.backendtest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDto {
    private String batchId;
    private LocalDateTime batchDate;
    private String batchFunction;
    private String batchName;
    private String fileLocation;
}
