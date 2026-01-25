package com.writingboard.server.global.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CodeExchangeRequest {

    @NotBlank(message = "코드는 필수입니다")
    @Pattern(
            regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
            message = "올바른 UUID 형식이 아닙니다"
    )
    private String code;
}
