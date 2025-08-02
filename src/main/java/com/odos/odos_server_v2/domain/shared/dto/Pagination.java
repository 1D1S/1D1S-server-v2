package com.odos.odos_server_v2.domain.shared.dto;

import java.util.List;

public class Pagination<T> {
  private List<T> items;
  private PageInfo pageInfo;
}
