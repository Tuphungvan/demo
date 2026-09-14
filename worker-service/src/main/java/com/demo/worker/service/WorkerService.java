package com.demo.worker.service;

import com.demo.worker.message.JobProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final JobProducer jobProducer;

    public void acceptJob(Integer jobId, String name, int subJobCount){
        for(int subJobIndex = 1; subJobIndex <= subJobCount; subJobIndex++){
            jobProducer.sendJobProcess(jobId, name, subJobCount, subJobIndex);
        }
    }
}
