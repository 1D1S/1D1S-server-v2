package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengePoke;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChallengePokeResponse {
  private Long pokedMemberId;
  private Boolean isPokedToday;

  public static ChallengePokeResponse from(ChallengePoke poke, Boolean isPoked) {
    return ChallengePokeResponse.builder()
        .pokedMemberId(poke.getReceiver().getId())
        .isPokedToday(isPoked)
        .build();
  }

  public static List<ChallengePokeResponse> from(List<ChallengePoke> poke, Boolean isPoked) {
    return poke.stream().map(p -> ChallengePokeResponse.from(p, isPoked)).toList();
  }
}
