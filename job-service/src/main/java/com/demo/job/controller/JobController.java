package com.demo.job.controller;

import com.demo.job.domain.Job;
import com.demo.job.service.JobService;
import com.demo.job.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final RateLimiterService rateLimiter;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestParam("name") String name,
            @RequestParam("subJobCount") @Min(1) int subJobCount,
            HttpServletRequest req) {

        String clientIp = req.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = req.getRemoteAddr();
        if (!rateLimiter.isAllowed(clientIp, 2, 60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("API rate limit exceeded");
        Job job = jobService.createJob(name, subJobCount);
        return ResponseEntity.ok(Map.of("message", "tạo job thành công, worker đang sử lý", "data", job));
    }

    @GetMapping
    public ResponseEntity<List<Job>> get() {
        return ResponseEntity.ok(jobService.getJob());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Job> update(@PathVariable("id") Integer id) {
        return ResponseEntity.accepted().body(jobService.updateJob(id));
    }
}
