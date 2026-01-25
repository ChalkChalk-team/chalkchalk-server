package com.writingboard.server.domain.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 리다이렉트 된 후 도착할 임시 정류장
    @GetMapping("/login/success-test")
    public String loginSuccessTest(@RequestParam String code) {
        return "<h1>로그인 성공! 🎉</h1>" +
                "<p>아래 코드를 복사해서 POST /api/auth/exchange 요청을 보내세요.</p>" +
                "<h2 style='color:blue; background:#eee; padding:10px;'>" + code + "</h2>";
    }
}