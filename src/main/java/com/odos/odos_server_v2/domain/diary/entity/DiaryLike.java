package com.odos.odos_server_v2.domain.diary.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table(name = "diary_like")
public class DiaryLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "diary_id")
  private Diary diary;

  public void setDiary(Diary diary) {
    diary.getLikes().add(this);
  }

  //    public Boolean likedByMe(Member member) {
  //        if (this.getMember().equals(member)) {
  //            return true;
  //        } else {
  //            return false;
  //        }
  //    }
  public boolean likedByMe(Member me) {
    if (this.member == null || me == null) return false;
    if (this.member.getId() == null || me.getId() == null) return false;
    return this.member.getId().equals(me.getId());
  }
}
