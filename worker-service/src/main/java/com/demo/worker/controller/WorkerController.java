package com.demo.worker.controller;

import com.demo.worker.service.WorkerService;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {
    private final WorkerService workerService;

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> acceptJob(
            @RequestParam("name") String name,
            @RequestParam("subJobCount") @Max(10000) int subJobCount,
            @PathVariable("id") Integer jobId
    ){
        workerService.acceptJob(jobId, name, subJobCount);
        return ResponseEntity.accepted().build();
    }
}
