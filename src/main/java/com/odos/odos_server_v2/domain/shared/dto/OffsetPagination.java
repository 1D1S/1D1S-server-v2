package com.odos.odos_server_v2.domain.shared.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class OffsetPagination<T> {

  private List<T> items;
  private PageInfo pageInfo;

  @Getter
  @AllArgsConstructor
  public static class PageInfo {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNextPage;
  }

  public static <T> OffsetPagination<T> from(Page<T> page) {
    return new OffsetPagination<>(
        page.getContent(),
        new PageInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()));
  }
}
