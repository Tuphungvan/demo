package com.demo.monitor.service;

import com.demo.monitor.domain.JobLog;
import com.demo.monitor.message.JobEvent;
import com.demo.monitor.repository.JobLogJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobLogService {

    private final JobLogJdbcRepository jobLogJdbcRepository;
    private final StringRedisTemplate template;
    private final RestClient client = RestClient.create("http://localhost:8000");
    private final TransactionTemplate transaction;

    /**
     * - Test put DLT topic
     *
     * @Transactional
     * public void processBatch(List<JobEvent> events) {
     * log.error("Lỗi hệ thống, retry và thêm vào DLT");
     * throw new RuntimeException("Giả lập lỗi hệ thống!");
     * }
     **/

    public void processBatch(List<JobEvent> events) {
        List<JobLog> logs = events.stream()
                .map(event -> JobLog.builder()
                        .jobId(event.jobId())
                        .name(event.name())
                        .subJobCount(event.subJobCount())
                        .subJob(event.subJob())
                        .build())
                .toList();
        transaction.executeWithoutResult(status -> jobLogJdbcRepository.batchInsert(logs));

        Set<Integer> completedJobIds = new HashSet<>();

        for (JobLog logItem : logs) {
            String redisKey = "job:" + logItem.getJobId() + ":subjobs";
            template.opsForSet().add(redisKey, String.valueOf(logItem.getSubJob()));

            Long completedCount = template.opsForSet().size(redisKey);
            if (completedCount != null && completedCount == logItem.getSubJobCount()) {
                completedJobIds.add(logItem.getJobId());
            }
        }

        for (Integer completedJobId : completedJobIds) {
            client.patch()
                    .uri("/api/jobs/{id}", completedJobId)
                    .retrieve()
                    .toBodilessEntity();
            template.delete("job:" + completedJobId + ":subjobs");
        }
    }
}
