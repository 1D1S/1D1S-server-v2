package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;

    public DiaryResponse createDiary(/*Long memberId, */DiaryRequest request) {
        //Member member = memberRepository.findById(memberId).orElseThrow();
        //Challenge challenge = challengeRepository.findById(reqeust.getChallengeId);

        Diary diary = Diary.builder()
                //.member()
                //.challenge()
                .completedDate(request.getAchievedDate())
                //.createdDate(LocalDate.now())
                .title(request.getTitle())
                .content(request.getContent())
                .feeling(request.getFeeling())
                .isPublic(request.getIsPublic())
                //.likes(null)
                //.diaryGoals(null)
                .build();

        diaryRepository.save(diary);
        return DiaryResponse.from(diary);
    }


}
