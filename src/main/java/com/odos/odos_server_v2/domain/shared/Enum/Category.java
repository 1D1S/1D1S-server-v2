package com.odos.odos_server_v2.domain.shared.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "카테고리 (DEV: 개발, EXERCISE: 운동, BOOK: 독서, DIET: 식단, HEALTH: 건강, HOBBY: 취미, LANGUAGE: 어학,"
            + " SELF_DEV: 자기계발, ETC: 기타)")
public enum Category {
  DEV,
  EXERCISE,
  BOOK,
  DIET,
  HEALTH,
  HOBBY,
  LANGUAGE,
  SELF_DEV,
  ETC
}
