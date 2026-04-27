package com.odos.odos_server_v2.domain.friend.entity;

import java.time.LocalDateTime;

import lombok.*;

import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "block_list", uniqueConstraints = {
    @UniqueConstraint(name = "uk_block_list_member_blocked", columnNames = {"member_id", "blocked_member_id"})
})
public class BlockList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_member_id", nullable = false)
    private Member blockedMember;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
