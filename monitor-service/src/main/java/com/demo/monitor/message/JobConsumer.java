package com.demo.monitor.message;

import com.demo.monitor.service.JobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobConsumer {
    private final JobLogService jobLogService;
    private final ObjectMapper objectMapper;
    private final DeadLetterPublishingRecoverer recoverer;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topics.job-logs}", concurrency = "3")
    public void consumer(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        List<JobEvent> events = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            try {
                JobEvent event = objectMapper.readValue(record.value(), JobEvent.class);
                if(event.subJob() <= 0 || event.jobId() == null) throw new IllegalArgumentException("Dữ liệu event bị sai");
                events.add(event);
            } catch (Exception ex){
                log.info("record {} đã được thêm vào dtl", record.value(), ex);
                recoverer.accept(record, ex);
            }
        }
        if(!events.isEmpty()) jobLogService.processBatch(events);
        ack.acknowledge();
    }
}

