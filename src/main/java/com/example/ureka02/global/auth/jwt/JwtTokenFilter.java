package com.example.ureka02.global.auth.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.ureka02.user.customUserDetails.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
	private final CustomUserDetailsService customUserDetailsService;
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request
			, HttpServletResponse response
			, FilterChain filterChain
			) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		String token = resolveToken(request);
		
        // 1. 토큰이 없으면 그냥 패스
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 이미 인증이 되어 있으면 다시 인증할 필요 없음(선택이지만 권장)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 만료 체크
        if (jwtTokenUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 토큰에서 id 추출
        long id = jwtTokenUtil.getId(token);

        // 5. UserDetails 조회
        UserDetails userDetails = customUserDetailsService.loadUserById(id);

        // 6. Authentication 생성 후 SecurityContext에 저장
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. 다음 필터로
        filterChain.doFilter(request, response);

		
	}
	
	private String resolveToken(HttpServletRequest request) {
        // 1) Authorization 헤더 (Bearer xxx)
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // "Bearer " 잘라내고 순수 토큰만 반환
            return authorizationHeader.substring(7);
        }

        // 2) accessToken 쿠키 (순수 토큰이 들어있다고 가정)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
	
}
