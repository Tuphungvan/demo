package com.demo.worker.message;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class JobProducer {
    @Value("${spring.kafka.topics.job-logs}")
    private String jobTopic;

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper mapper;

    public void sendJobProcess(Integer jobId, String name, int subJobCount, int subJob){
        JobEvent event = new JobEvent(jobId, name, subJobCount, subJob);
        String json = mapper.writeValueAsString(event);
        template.send(jobTopic, jobId.toString(), json);
    }

}
