package jnu.econovation.ecoknockbecentral.group.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupApplication extends BaseEntity {

    public static final int MIN_CONTENT_LENGTH = 1;
    public static final int MAX_CONTENT_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Member applicant;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupApplicationStatus status;

    private GroupApplication(Group group, Member applicant, String content) {
        this.group = Objects.requireNonNull(group, "Group must not be null");
        this.applicant = Objects.requireNonNull(applicant, "Applicant must not be null");
        this.content = requireContent(content);
        this.status = GroupApplicationStatus.PENDING;
    }

    public static GroupApplication pending(Group group, Member applicant, String content) {
        return new GroupApplication(group, applicant, content);
    }

    public void accept() {
        requirePending();
        this.status = GroupApplicationStatus.ACCEPTED;
    }

    public void reject() {
        requirePending();
        this.status = GroupApplicationStatus.REJECTED;
    }

    private void requirePending() {
        if (status != GroupApplicationStatus.PENDING) {
            throw new IllegalStateException("Only pending group application can be processed");
        }
    }

    private static String requireContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException("Group application content must not be null");
        }
        String trimmed = content.trim();
        if (trimmed.length() < MIN_CONTENT_LENGTH || trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Group application content length must be between "
                            + MIN_CONTENT_LENGTH
                            + " and "
                            + MAX_CONTENT_LENGTH
            );
        }
        return trimmed;
    }
}
