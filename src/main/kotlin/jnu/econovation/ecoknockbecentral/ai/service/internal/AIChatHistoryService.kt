package jnu.econovation.ecoknockbecentral.ai.service.internal

import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.RawAIServerResponseDTO
import jnu.econovation.ecoknockbecentral.ai.dto.internal.AIChatHistoryDTO
import jnu.econovation.ecoknockbecentral.ai.dto.rest.response.AIChatHistoryPageResponse
import jnu.econovation.ecoknockbecentral.ai.dto.rest.response.AIChatHistoryResponse
import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import jnu.econovation.ecoknockbecentral.ai.repository.AIChatHistoryRepository
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AIChatHistoryService(
    private val memberService: MemberService,
    private val repository: AIChatHistoryRepository
) {
    @Transactional
    fun saveChat(
        memberInfo: MemberInfoDTO,
        question: String,
        answer: String,
        rawResponse: RawAIServerResponseDTO,
    ) {
        val member = memberService.getEntityOrThrow(memberInfo.id)

        repository.save(
            AIChatHistory.builder()
                .member(member)
                .question(question)
                .answer(answer)
                .rawResponse(rawResponse)
                .build()
        )
    }

    @Transactional(readOnly = true)
    fun getChatHistory(memberInfo: MemberInfoDTO): List<AIChatHistoryDTO> {
        return repository
            .findTop20ByMemberIdOrderByCreatedAtDescIdDesc(memberInfo.id)
            .take(20)
            .asReversed()
            .map(AIChatHistoryDTO::from)
    }

    @Transactional(readOnly = true)
    fun getChatHistoryPage(
        memberInfo: MemberInfoDTO,
        limit: Int,
        before: Instant?,
    ): AIChatHistoryPageResponse {
        val pageable = PageRequest.of(0, limit + 1)
        val histories = if (before == null) {
            repository.findByMemberIdOrderByCreatedAtDescIdDesc(memberInfo.id, pageable)
        } else {
            repository.findByMemberIdAndCreatedAtBeforeOrderByCreatedAtDescIdDesc(
                memberInfo.id,
                before,
                pageable,
            )
        }

        val hasNext = histories.size > limit
        val responses = histories.take(limit).map(AIChatHistoryResponse::from)
        val nextBefore = if (hasNext) {
            responses.lastOrNull()?.createdAt
                ?: throw InternalServerException(IllegalStateException("AI 채팅 이력의 createdAt이 null입니다."))
        } else {
            null
        }

        return AIChatHistoryPageResponse(
            items = responses,
            hasNext = hasNext,
            nextBefore = nextBefore,
        )
    }
}
