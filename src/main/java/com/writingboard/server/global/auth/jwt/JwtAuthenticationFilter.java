package com.writingboard.server.global.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출 (Bearer ...)
        String token = resolveToken(request);

        // 2. 토큰 유효성 검사
        if (token != null && jwtProvider.validateToken(token)) {

            // 3. 토큰에서 사용자 ID(PK) 꺼내기
            Long memberId = jwtProvider.getMemberIdFromToken(token);

            // 4. 인증 객체(Authentication) 생성
            // (권한은 일단 ROLE_USER로 고정, 필요하면 토큰에서 role 꺼내도록 수정 가능)
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, null, authorities);

            // 5. 스프링 시큐리티 컨텍스트에 저장 (이제부터 이 요청은 '인증된 사용자'로 처리됨)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("✅ 인증 성공: memberId={}", memberId);
        }

        // 6. 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 "Authorization" 값을 꺼내고 "Bearer " 접두사를 제거함
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열만 리턴
        }

        return null;
    }
}