package com.demo.job.massage;

import com.demo.job.domain.Job;
import com.demo.job.domain.JobStatus;
import com.demo.job.repository.JobRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDtlConsumer {
    private final ObjectMapper mapper;
    private final JobRepo jobRepo;
    private final StringRedisTemplate template;

    @KafkaListener(groupId = "job-group.DLT", topics = "job-logs.DLT", concurrency = "3")
    public void consumer(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        Set<Integer> jobFaildes = new HashSet<>();
        for (ConsumerRecord<String, String> record : records) {
            try {
                JobEvent event = mapper.readValue(record.value(), JobEvent.class);
                jobFaildes.add(event.jobId());
            } catch (Exception e) {
                log.info("Đã có lỗi xảy ra khi parse record{}", record.value(), e);
            }
        }
        boolean changed = false;
        for (Integer id : jobFaildes) {
            int updated = jobRepo.updateStatusToFailed(id);
            if (updated > 0) {
                changed = true;
                log.info("Job {} đã được chuyển sang trạng thái FAILED từ DLT", id);
            }
        }
        if (changed) template.delete("job:all");
        ack.acknowledge();
    }
}
