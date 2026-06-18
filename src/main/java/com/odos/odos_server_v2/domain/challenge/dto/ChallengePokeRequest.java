package com.odos.odos_server_v2.domain.challenge.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengePokeRequest {
  private List<Long> receiverMemberIds;
}
