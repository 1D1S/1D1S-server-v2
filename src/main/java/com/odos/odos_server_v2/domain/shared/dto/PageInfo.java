package com.odos.odos_server_v2.domain.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
  private Long limit;
  private boolean hasNextPage;
  private String nextCursor;
}
