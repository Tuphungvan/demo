package com.demo.job.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer jobId;

    private String name;

    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus = JobStatus.CREATE;

    private int subJobCount;
}
