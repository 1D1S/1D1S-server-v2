package com.odos.odos_server_v2.domain.shared.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "카테고리 (DEV: 개발, EXERCISE: 운동, BOOK: 독서, MUSIC: 음악, STUDY: 공부, LEISURE: 여가, ECONOMY: 경제)")
public enum Category {
  DEV,
  EXERCISE,
  BOOK,
  MUSIC,
  STUDY,
  LEISURE,
  ECONOMY
}
