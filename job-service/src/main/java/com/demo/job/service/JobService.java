package com.demo.job.service;

import com.demo.job.domain.Job;
import com.demo.job.domain.JobStatus;
import com.demo.job.repository.JobRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final StringRedisTemplate template;
    private final JobRepo jobRepo;
    private final RestClient restClient = RestClient.create("http://localhost:8000");
    private final String key = "job:all";
    private final ObjectMapper objectMapper;

    public Job createJob(String name, int subJobCount) {

        Job job = jobRepo.save(
                Job.builder()
                        .name(name)
                        .jobStatus(JobStatus.CREATE)
                        .subJobCount(subJobCount)
                        .build()
        );

        template.delete(key);

        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/worker/{id}/start")
                            .queryParam("name", name)
                            .queryParam("subJobCount", subJobCount)
                            .build(job.getJobId()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex){
            jobRepo.updateStatusToFailed(job.getJobId());
            log.info("Worker không thể xử lý job {}, đánh dấu FAILED", job.getJobId(), ex);
            throw new RuntimeException("Giao việc cho worker thất bại", ex);
        }
        jobRepo.updateStatusToProcess(job.getJobId());
        return jobRepo.findById(job.getJobId()).orElse(job);
    }

    @Transactional
    public Job updateJob(Integer id){
        jobRepo.updateStatusToDone(id);
        template.delete(key);
        return jobRepo.findById(id).orElseThrow(() -> new RuntimeException("job khong ton tai"));
    }

    public List<Job> getJob(){
        String cache = template.opsForValue().get(key);
        if(cache != null) return objectMapper.readValue(cache, new TypeReference<>() {});
        List<Job> jobs = jobRepo.findAll();
        String json = objectMapper.writeValueAsString(jobs);
        template.opsForValue().set(key, json, Duration.ofMinutes(10));
        return jobs;
    }
}

