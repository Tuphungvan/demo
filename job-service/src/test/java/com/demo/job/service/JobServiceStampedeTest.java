package com.demo.job.service;

import com.demo.job.domain.Job;
import com.demo.job.repository.JobRepo;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class JobServiceStampedeTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private StringRedisTemplate template;

    @MockitoSpyBean
    private JobRepo jobRepository;

    @BeforeEach
    void setUp(){
        template.delete("job:all");
        template.delete("lock:job:all");
    }

    @Test
    @DisplayName("Giả lập chống sập db, chỉ được query db 1 lần")
    void testCacheStampedeProtection() throws InterruptedException {
        int numberOfThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    List<Job> jobs = jobService.getJob();
                    if (jobs != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(20, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(numberOfThreads, successCount.get(), "Tất cả thread phải nhận được kết quả!");
        verify(jobRepository, times(1)).findAll();
    }
}
