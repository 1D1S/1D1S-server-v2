package com.odos.odos_server_v2.domain.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedisTestController {
  private final StringRedisTemplate redisTemplate;

  @GetMapping("/redis-test")
  public String redisTest() {

    redisTemplate.opsForValue().set("test", "hello");

    return redisTemplate.opsForValue().get("test");
  }
}
