package com.writingboard.server.domain.drawing.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DrawingSnapshotRequest {

    @NotNull(message = "페이지 인덱스는 필수입니다")
    @Min(value = 0, message = "페이지 인덱스는 0 이상이어야 합니다")
    @Max(value = 999, message = "페이지 인덱스는 999 이하여야 합니다")
    private Integer pageIndex;

    @NotNull(message = "마지막 포함 버전은 필수입니다")
    @Min(value = 1, message = "버전은 1 이상이어야 합니다")
    private Long lastIncludedVersion;

    @NotBlank(message = "스냅샷 데이터는 필수입니다")
    @Size(max = 500000, message = "스냅샷 데이터는 최대 500000자까지 가능합니다")
    private String snapshotData;
}
