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
@Table(name = "diary_report")
public class DiaryReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  @Enumerated(EnumType.STRING)
  private ReportType type;

  @Column private String content;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "diary_id")
  private Diary diary;
}
