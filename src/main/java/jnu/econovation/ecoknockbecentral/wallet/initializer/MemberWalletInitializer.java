package jnu.econovation.ecoknockbecentral.wallet.initializer;

import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberWalletInitializer {
    private final MemberWalletService memberWalletService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        int createdCount = memberWalletService.createManagedWalletsForExistingMembers();
        if (createdCount > 0) {
            log.info("Created managed wallets for {} existing members", createdCount);
        }
    }
}
