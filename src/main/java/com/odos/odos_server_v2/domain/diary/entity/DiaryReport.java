package com.odos.odos_server_v2.domain.diary.entity;

import com.odos.odos_server_v2.domain.diary.entity.Enum.ReportType;
import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "DiaryReport")
public class DiaryReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  @Enumerated(EnumType.STRING)
  private ReportType type;

  @Column private String content;

  @ManyToOne
  @JoinColumn(name = "memberId")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "diaryId")
  private Diary diary;
}
