package com.odos.odos_server_v2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
  @GetMapping("/")
  public String index() {
    return "서버 정상 작동 중!";
  }
}
