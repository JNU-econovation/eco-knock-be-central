package jnu.econovation.ecoknockbecentral.wallet.event;

import jnu.econovation.ecoknockbecentral.member.event.MemberCreatedEvent;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MemberCreatedWalletEventListener {

    private final MemberWalletService memberWalletService;

    //지갑이 없는 회원은 있으면 안된다.
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createManagedWallet(MemberCreatedEvent event) {
        // 게스트 회원가입 시 지갑 생성 차단
        if (event.getRole() == Role.GUEST) {
            return;
        }

        memberWalletService.createManagedWalletIfAbsent(event.getMemberId());
    }
}
