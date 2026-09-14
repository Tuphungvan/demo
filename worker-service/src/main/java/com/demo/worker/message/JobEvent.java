package com.demo.worker.message;

public record JobEvent(Integer jobId, String name, int subJobCount, int subJob) {
}
