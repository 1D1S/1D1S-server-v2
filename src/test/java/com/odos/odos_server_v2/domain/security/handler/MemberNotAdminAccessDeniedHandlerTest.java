package com.odos.odos_server_v2.domain.security.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odos.odos_server_v2.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class MemberNotAdminAccessDeniedHandlerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final MemberNotAdminAccessDeniedHandler handler =
      new MemberNotAdminAccessDeniedHandler(objectMapper);

  @Test
  void returnsMemberNotAdminErrorResponse() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.handle(
        new MockHttpServletRequest(), response, new AccessDeniedException("Access denied"));

    ErrorCode errorCode = ErrorCode.MEMBER_NOT_ADMIN;
    assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
    assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
    assertThat(response.getContentAsString())
        .isEqualTo(
            objectMapper.writeValueAsString(
                new ExpectedErrorResponse(errorCode.getCode(), errorCode.getMessage())));
  }

  private record ExpectedErrorResponse(String code, String message) {}
}
