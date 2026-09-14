package com.demo.monitor.repository;

import com.demo.monitor.domain.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobLogRepository extends JpaRepository<JobLog, Long> {
}
