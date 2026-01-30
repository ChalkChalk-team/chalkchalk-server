package com.writingboard.server.domain.drawing.dto.request;

import com.writingboard.server.domain.drawing.enums.DrawingMessageType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 드로잉 스트로크 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DrawingStrokeRequest {

    @NotNull(message = "메시지 타입은 필수입니다")
    private DrawingMessageType type;

    @NotNull(message = "페이지 인덱스는 필수입니다")
    @Min(value = 0, message = "페이지 인덱스는 0 이상이어야 합니다")
    @Max(value = 999, message = "페이지 인덱스는 999 이하여야 합니다")
    private Integer pageIndex;

    @Size(max = 100, message = "스트로크 ID는 최대 100자까지 가능합니다")
    private String strokeId;

    @Size(max = 100000, message = "스트로크 데이터는 최대 100000자까지 가능합니다")
    private String strokeData;

    private Long version;
}
