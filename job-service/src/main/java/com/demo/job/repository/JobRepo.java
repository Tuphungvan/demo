package com.demo.job.repository;

import com.demo.job.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JobRepo extends JpaRepository<Job, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.jobStatus = 'PROCESS' WHERE j.jobId = :id AND j.jobStatus = 'CREATE'")
    int updateStatusToProcess(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.jobStatus = 'DONE' WHERE j.jobId = :id AND j.jobStatus IN ('CREATE', 'PROCESS')")
    int updateStatusToDone(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.jobStatus = 'FAILED' WHERE j.jobId = :id AND j.jobStatus != 'DONE'")
    int updateStatusToFailed(@Param("id") Integer id);
}
