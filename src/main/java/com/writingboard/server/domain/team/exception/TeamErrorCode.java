package com.writingboard.server.domain.team.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode {

    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_001", "팀을 찾을 수 없습니다"),
    TEAM_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "TEAM_002", "이미 존재하는 팀 이름입니다"),

    // Team Member - 권한 관련
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "TEAM_101", "팀 멤버가 아닙니다"),
    NOT_TEAM_ADMIN(HttpStatus.FORBIDDEN, "TEAM_102", "관리자 권한이 필요합니다"),
    NOT_TEAM_OWNER(HttpStatus.FORBIDDEN, "TEAM_103", "팀 소유자만 가능합니다"),
    CANNOT_CHANGE_OWNER_ROLE(HttpStatus.BAD_REQUEST, "TEAM_104", "소유자 권한은 변경할 수 없습니다"),
    CANNOT_DEMOTE_SELF(HttpStatus.BAD_REQUEST, "TEAM_105", "자신의 권한은 강등할 수 없습니다"),
    OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "TEAM_106", "팀 소유자는 팀을 탈퇴할 수 없습니다"),
    CANNOT_KICK_OWNER(HttpStatus.BAD_REQUEST, "TEAM_107", "팀 소유자는 강퇴할 수 없습니다"),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "TEAM_108", "자기 자신은 강퇴할 수 없습니다"),

    // Team Invitation
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_201", "초대를 찾을 수 없습니다"),
    INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, "TEAM_202", "만료된 초대입니다"),
    ALREADY_TEAM_MEMBER(HttpStatus.BAD_REQUEST, "TEAM_203", "이미 팀 멤버입니다"),
    ALREADY_INVITED(HttpStatus.BAD_REQUEST, "TEAM_204", "이미 초대된 사용자입니다"),
    INVALID_INVITATION(HttpStatus.BAD_REQUEST, "TEAM_205", "유효하지 않은 초대입니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_301", "사용자를 찾을 수 없습니다"),

    // Team Asset
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_401", "자산을 찾을 수 없습니다"),
    ASSET_MODIFY_FORBIDDEN(HttpStatus.FORBIDDEN, "TEAM_402", "자산을 수정/삭제할 권한이 없습니다"),
    ASSET_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "TEAM_403", "이미 삭제된 자산입니다"),
    INVALID_ASSET_TYPE(HttpStatus.BAD_REQUEST, "TEAM_404", "지원하지 않는 파일 타입입니다"),
    PREVIEW_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TEAM_405", "preview 이미지 업로드에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
