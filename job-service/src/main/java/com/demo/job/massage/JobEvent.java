package com.demo.job.massage;

public record JobEvent(Integer jobId, String name, int subJobCount, int subJob) {
}
