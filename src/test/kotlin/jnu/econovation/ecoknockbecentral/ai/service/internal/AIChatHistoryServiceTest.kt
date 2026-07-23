package jnu.econovation.ecoknockbecentral.ai.service.internal

import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import jnu.econovation.ecoknockbecentral.ai.repository.AIChatHistoryRepository
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageRequest
import java.time.Instant

class AIChatHistoryServiceTest {
    private val memberService = mock(MemberService::class.java)
    private val repository = mock(AIChatHistoryRepository::class.java)
    private val service = AIChatHistoryService(memberService, repository)
    private val memberInfo = MemberInfoDTO(
        1L,
        100L,
        Role.USER,
        Cohort(1),
        "테스트 회원",
        ActiveStatus.OB,
    )

    @Test
    fun `최근 이력을 DTO로 변환하고 오래된 순서로 반환한다`() {
        val histories = (20 downTo 1).map { chatHistory("질문$it", "답변$it") }
        `when`(repository.findTop20ByMemberIdOrderByCreatedAtDescIdDesc(memberInfo.id)).thenReturn(histories)

        val result = service.getChatHistory(memberInfo)

        assertThat(result).extracting<String> { it.question }
            .containsExactlyElementsOf((1..20).map { "질문$it" })
    }

    @Test
    fun `과거 이력은 최신순으로 limit만 반환하고 다음 커서를 제공한다`() {
        val newest = Instant.parse("2026-07-23T12:30:00Z")
        val histories = (0..2).map { index ->
            chatHistory(
                id = (index + 1).toLong(),
                question = "질문${index + 1}",
                answer = "답변${index + 1}",
                createdAt = newest.minusSeconds(index.toLong() * 60),
            )
        }
        `when`(repository.findByMemberIdOrderByCreatedAtDescIdDesc(memberInfo.id, PageRequest.of(0, 3)))
            .thenReturn(histories)

        val result = service.getChatHistoryPage(memberInfo, limit = 2, before = null)

        assertThat(result.items).extracting<String> { it.question }
            .containsExactly("질문1", "질문2")
        assertThat(result.hasNext).isTrue()
        assertThat(result.nextBefore).isEqualTo(newest.minusSeconds(60))
    }

    @Test
    fun `before 커서는 해당 시각보다 오래된 회원 이력만 조회한다`() {
        val before = Instant.parse("2026-07-23T12:30:00Z")
        val histories = listOf(
            chatHistory("질문3", "답변3", 3L, Instant.parse("2026-07-23T12:28:00Z")),
            chatHistory("질문4", "답변4", 4L, Instant.parse("2026-07-23T12:27:00Z")),
        )
        `when`(
            repository.findByMemberIdAndCreatedAtBeforeOrderByCreatedAtDescIdDesc(
                memberInfo.id,
                before,
                PageRequest.of(0, 3),
            )
        ).thenReturn(histories)

        val result = service.getChatHistoryPage(memberInfo, limit = 2, before = before)

        assertThat(result.items).extracting<String> { it.question }
            .containsExactly("질문3", "질문4")
        assertThat(result.hasNext).isFalse()
        assertThat(result.nextBefore).isNull()
    }

    private fun chatHistory(
        question: String,
        answer: String,
        id: Long = 1L,
        createdAt: Instant = Instant.parse("2026-07-23T12:00:00Z"),
    ): AIChatHistory {
        return mock(AIChatHistory::class.java).also {
            `when`(it.id).thenReturn(id)
            `when`(it.question).thenReturn(question)
            `when`(it.answer).thenReturn(answer)
            `when`(it.createdAt).thenReturn(createdAt)
        }
    }
}
