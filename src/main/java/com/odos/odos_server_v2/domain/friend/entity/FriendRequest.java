package com.odos.odos_server_v2.domain.friend.entity;

import java.time.LocalDateTime;

import lombok.*;

import com.odos.odos_server_v2.domain.friend.entity.Enum.FriendRequestStatus;
import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "friend_request", uniqueConstraints = {
    @UniqueConstraint(name = "uk_friend_request_from_to", columnNames = {"from_member_id", "to_member_id"})
})
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = FriendRequestStatus.CANCELED;
    }

    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
    }
}
