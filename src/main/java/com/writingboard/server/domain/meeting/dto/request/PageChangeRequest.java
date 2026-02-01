package com.writingboard.server.domain.meeting.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageChangeRequest {

    @NotNull(message = "페이지 번호는 필수입니다")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer pageNumber;
}
