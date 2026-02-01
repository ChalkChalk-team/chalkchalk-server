package com.writingboard.server.domain.personal.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.personal.dto.request.PersonalAssetCreateRequest;
import com.writingboard.server.domain.personal.dto.response.PersonalAssetResponse;
import com.writingboard.server.domain.personal.entity.PersonalAsset;
import com.writingboard.server.domain.personal.exception.PersonalErrorCode;
import com.writingboard.server.domain.personal.exception.PersonalException;
import com.writingboard.server.domain.personal.repository.PersonalAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalAssetService {

    private final PersonalAssetRepository personalAssetRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PersonalAssetResponse createAsset(Long memberId, PersonalAssetCreateRequest request) {
        Member member = getMemberById(memberId);

        PersonalAsset asset = PersonalAsset.create(
                member,
                request.getName(),
                request.getType(),
                request.getStorageKey(),
                request.getSizeBytes(),
                request.getTotalPages()
        );

        personalAssetRepository.save(asset);
        return PersonalAssetResponse.from(asset);
    }

    public Page<PersonalAssetResponse> getAssets(Long memberId, Pageable pageable) {
        return personalAssetRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable)
                .map(PersonalAssetResponse::from);
    }

    public PersonalAssetResponse getAsset(Long memberId, Long assetId) {
        PersonalAsset asset = getAssetWithOwnershipCheck(assetId, memberId);
        return PersonalAssetResponse.from(asset);
    }

    @Transactional
    public void deleteAsset(Long memberId, Long assetId) {
        PersonalAsset asset = getAssetWithOwnershipCheck(assetId, memberId);
        asset.softDelete();
    }

    public PersonalAsset getAssetEntity(Long assetId) {
        return personalAssetRepository.findByIdAndDeletedAtIsNull(assetId)
                .orElseThrow(() -> new PersonalException(PersonalErrorCode.ASSET_NOT_FOUND));
    }

    private PersonalAsset getAssetWithOwnershipCheck(Long assetId, Long memberId) {
        PersonalAsset asset = personalAssetRepository.findByIdAndDeletedAtIsNull(assetId)
                .orElseThrow(() -> new PersonalException(PersonalErrorCode.ASSET_NOT_FOUND));

        if (!asset.isOwnedBy(memberId)) {
            throw new PersonalException(PersonalErrorCode.ASSET_ACCESS_DENIED);
        }

        return asset;
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new PersonalException(PersonalErrorCode.ASSET_NOT_FOUND));
    }
}
