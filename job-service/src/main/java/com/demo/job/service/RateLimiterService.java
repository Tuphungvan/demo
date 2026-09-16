package com.demo.job.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate template;

    private static final String RATE_LIMIT_LUA = """
            local current = redis.call('INCR', KEYS[1])
            if tonumber(current) == 1 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
            end
            if tonumber(current) > tonumber(ARGV[2]) then
                return 0
            else
                return 1
            end
            """;

    private final RedisScript<Long> script = RedisScript.of(RATE_LIMIT_LUA, Long.class);
    public boolean isAllowed(String ip, int limit, int time){
        String key = "rate:limit:" + ip;
        Long result = template.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(time),
                String.valueOf(limit)
        );
        return result != null && result == 1L;
    }
}
