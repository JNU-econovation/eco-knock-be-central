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

    List<GroupApplication> findAllByGroupIdAndStatus(
            Long groupId,
            GroupApplicationStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT application
            FROM GroupApplication application
            WHERE application.id = :applicationId
            """)
    Optional<GroupApplication> findByIdForUpdate(
            @Param("applicationId") Long applicationId
    );
}
