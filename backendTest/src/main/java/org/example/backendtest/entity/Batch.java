package org.example.backendtest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "batch_date")
    private LocalDateTime batchDate;

    @Column(name = "batch_function")
    private String batchFunction;

    @Column(name = "batch_name")
    private String batchName;

    @Column(name = "file_location")
    private String fileLocation;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "source_id")
    private Long sourceId;
}
