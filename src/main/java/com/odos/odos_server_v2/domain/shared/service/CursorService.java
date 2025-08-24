package com.odos.odos_server_v2.domain.shared.service;

import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class CursorService {
  public String encodeCursor(Long id) {
    return Base64.getEncoder().encodeToString(("cursor:" + id).getBytes());
  }

  public Long decodeCursorToId(String cursor) {
    if (cursor == null) return null;
    try {
      String decoded = new String(Base64.getDecoder().decode(cursor));
      return Long.parseLong(decoded.split(":")[1]);
    } catch (Exception e) {
      return null;
    }
  }
}
