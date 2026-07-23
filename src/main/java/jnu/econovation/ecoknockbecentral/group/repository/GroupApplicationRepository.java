package jnu.econovation.ecoknockbecentral.group.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupApplicationRepository extends JpaRepository<GroupApplication, Long> {

    boolean existsByGroupIdAndApplicantIdAndStatus(
            Long groupId,
            Long applicantId,
            GroupApplicationStatus status
    );

    @Query("""
            SELECT application
            FROM GroupApplication application
            JOIN FETCH application.applicant
            WHERE application.group.id = :groupId
              AND application.status = :status
            ORDER BY application.createdAt ASC, application.id ASC
            """)
    List<GroupApplication> findAllByGroupIdAndStatusWithApplicant(
            @Param("groupId") Long groupId,
            @Param("status") GroupApplicationStatus status
    );

    @Query("""
            SELECT application
            FROM GroupApplication application
            JOIN FETCH application.applicant
            WHERE application.id = :applicationId
              AND application.group.id = :groupId
              AND application.status = :status
            """)
    Optional<GroupApplication> findByIdAndGroupIdAndStatusWithApplicant(
            @Param("applicationId") Long applicationId,
            @Param("groupId") Long groupId,
            @Param("status") GroupApplicationStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT application
            FROM GroupApplication application
            WHERE application.id = :applicationId
              AND application.group.id = :groupId
            """)
    Optional<GroupApplication> findByIdAndGroupIdForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("groupId") Long groupId
    );
}
