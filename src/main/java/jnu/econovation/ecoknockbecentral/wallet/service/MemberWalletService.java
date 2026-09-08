package jnu.econovation.ecoknockbecentral.wallet.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import jnu.econovation.ecoknockbecentral.wallet.security.WalletPrivateKeyCipher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberWalletService {

    private static final int PRIVATE_KEY_HEX_LENGTH = 64;  // EVM Private Key: 256bit -> Hex * 16

    private final EntityManager entityManager;
    private final MemberRepository memberRepository;
    private final MemberWalletRepository memberWalletRepository;
    private final WalletPrivateKeyCipher privateKeyCipher;

    @Transactional
    public MemberWallet createManagedWalletIfAbsent(Long memberId) {
        Member member = findMemberWithLock(memberId);

        return memberWalletRepository
                .findByMemberIdAndWalletType(memberId, WalletType.MANAGED)
                .orElseGet(() -> createAndSaveManagedWallet(member));
    }

    @Transactional
    public int createManagedWalletsForExistingMembers() {
        List<Long> memberIds = memberRepository.findAll().stream()
                .filter(member -> member.getRole() != Role.GUEST) //이미 존재하는 회원 중 게스트는 지갑 생성 차단
                .map(Member::getId)
                .sorted(Comparator.naturalOrder())
                .toList();
        int createdCount = 0;

        for (Long memberId : memberIds) {
            Member member = findMemberWithLock(memberId);
            boolean managedWalletExists = memberWalletRepository.findByMemberIdAndWalletType(
                    memberId,
                    WalletType.MANAGED
            ).isPresent();
            if (managedWalletExists) {
                continue;
            }

            boolean activeRewardDestination = !memberWalletRepository
                    .existsByMemberIdAndActiveRewardDestinationTrue(memberId);
            memberWalletRepository.save(createManagedWallet(member, activeRewardDestination));
            createdCount++;
        }

        return createdCount;
    }

    @Transactional(readOnly = true)
    public Optional<MemberWallet> findActiveRewardWallet(Long memberId) {
        return memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(memberId);
    }

    private Member findMemberWithLock(Long memberId) {
        Member member = entityManager.find(Member.class, memberId, LockModeType.PESSIMISTIC_WRITE);
        if (member == null) {
            throw new InternalServerException(
                    new IllegalStateException("Member not found: " + memberId)
            );
        }
        return member;
    }

    private MemberWallet createAndSaveManagedWallet(Member member) {
        boolean activeRewardDestination = !memberWalletRepository
                .existsByMemberIdAndActiveRewardDestinationTrue(
                        member.getId()
                );

        return memberWalletRepository.save(
                createManagedWallet(member, activeRewardDestination)
        );
    }


    private MemberWallet createManagedWallet(
            Member member,
            boolean activeRewardDestination
    ) {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();

            String privateKey = Numeric.toHexStringWithPrefixZeroPadded(
                    keyPair.getPrivateKey(),
                    PRIVATE_KEY_HEX_LENGTH
            );

            String walletAddress = Credentials.create(keyPair)
                    .getAddress()
                    .toLowerCase(Locale.ROOT);

            return MemberWallet.managed(
                    member,
                    walletAddress,
                    privateKeyCipher.encrypt(privateKey),
                    activeRewardDestination
            );
        } catch (GeneralSecurityException exception) {
            throw new InternalServerException(exception);
        }
    }
}
