package com.odos.odos_server_v2.domain.shared.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagination<T> {
  private List<T> items;
  private PageInfo pageInfo;
}
