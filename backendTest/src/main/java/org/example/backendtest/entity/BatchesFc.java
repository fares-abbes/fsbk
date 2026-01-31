package org.example.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batches_fc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchesFc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "batch_date")
    private LocalDateTime batchDate;

    @Column(name = "batch_end_date")
    private LocalDateTime batchEndDate;

    @Column(name = "batch_function")
    private String batchFunction;

    @Column(name = "batch_last_execution")
    private LocalDateTime batchLastExecution;

    @Column(name = "batch_libelle")
    private String batchLibelle;

    @Column(name = "batch_name")
    private String batchName;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "batch_status")
    private String batchStatus;

    @Column(name = "batch_type_fb")
    private String batchTypeFb;

    @Column(name = "date_reg")
    private LocalDateTime dateReg;

    @Column(name = "error")
    private String error;

    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;

    @Column(name = "file_location")
    private String fileLocation;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "keyfc")
    private String keyfc;
}
