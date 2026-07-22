package com.odos.odos_server_v2.domain.push.dto;

import java.util.HashMap;
import java.util.Map;

/** 앱 푸시 1건. data는 앱이 탭했을 때 이동할 {@code path} 등 커스텀 페이로드. */
public record PushMessage(String title, String body, Map<String, String> data) {

  public static PushMessage of(String title, String body, String path) {
    Map<String, String> data = new HashMap<>();
    if (path != null) {
      data.put("path", path);
    }
    return new PushMessage(title, body, data);
  }
}
