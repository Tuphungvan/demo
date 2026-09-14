package com.demo.monitor.message;

public record JobEvent(Integer jobId, String name, int subJobCount, int subJob) {
}
