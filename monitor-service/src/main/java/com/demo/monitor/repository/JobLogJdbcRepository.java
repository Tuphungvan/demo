package com.demo.monitor.repository;

import com.demo.monitor.domain.JobLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JobLogJdbcRepository {

    private static final String BATCH_INSERT_SQL = """
            INSERT INTO job_logs (
                id,
                job_id,
                name,
                sub_job_count,
                sub_job,
                received_at
            )
            VALUES (nextval('job_logs_seq'), ?, ?, ?, ?, ?)
            ON CONFLICT (job_id, sub_job) DO NOTHING
            """;

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<JobLog> logs) {
        jdbcTemplate.batchUpdate(
                BATCH_INSERT_SQL,
                logs,
                logs.size(),
                (PreparedStatement ps, JobLog logItem) -> {
                    ps.setInt(1, logItem.getJobId());
                    ps.setString(2, logItem.getName());
                    ps.setInt(3, logItem.getSubJobCount());
                    ps.setInt(4, logItem.getSubJob());
                    ps.setTimestamp(5, Timestamp.from(logItem.getReceivedAt()));
                }
        );
    }
}
