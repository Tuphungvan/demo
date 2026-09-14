package com.demo.job.controller;

import com.demo.job.domain.Job;
import com.demo.job.service.JobService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam("name") String name,
            @RequestParam("subJobCount") @Min(1) int subJobCount) {
        Job job = jobService.createJob(name, subJobCount);
        return ResponseEntity.ok(Map.of("message", "tạo job thành công, worker đang sử lý", "data", job));
    }

    @GetMapping
    public ResponseEntity<List<Job>> get(){
        return ResponseEntity.ok(jobService.getJob());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Job> update(@PathVariable("id") Integer id){
        return ResponseEntity.accepted().body(jobService.updateJob(id));
    }
}
