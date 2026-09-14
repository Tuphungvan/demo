package com.demo.worker.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.topics.job-logs}")
    private String jobTopic;

    @Bean
    public NewTopic jobLogTopic() {
        return TopicBuilder.name(jobTopic).partitions(3).replicas(1).build();
    }
}
