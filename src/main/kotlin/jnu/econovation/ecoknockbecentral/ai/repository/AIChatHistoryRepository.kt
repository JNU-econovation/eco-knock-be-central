package jnu.econovation.ecoknockbecentral.ai.repository

import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AIChatHistoryRepository : JpaRepository<AIChatHistory, Long> {
    fun findTop20ByMemberIdOrderByCreatedAtDescIdDesc(memberId: Long): List<AIChatHistory>

    fun findByMemberIdOrderByCreatedAtDescIdDesc(
        memberId: Long,
        pageable: Pageable,
    ): List<AIChatHistory>

    fun findByMemberIdAndCreatedAtBeforeOrderByCreatedAtDescIdDesc(
        memberId: Long,
        before: Instant,
        pageable: Pageable,
    ): List<AIChatHistory>
}
