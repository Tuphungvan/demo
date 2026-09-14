package com.demo.monitor.domain;

import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;

@Entity
@Table(name = "job_logs", uniqueConstraints = {@UniqueConstraint(name = "uk_job_log_event", columnNames = {"jobId", "subJob"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Integer jobId;

    private String name;

    private int subJobCount;

    private int subJob;

    @Builder.Default
    private Instant receivedAt = Instant.now();
}
