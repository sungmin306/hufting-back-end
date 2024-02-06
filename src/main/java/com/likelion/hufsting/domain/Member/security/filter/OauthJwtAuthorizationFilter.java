package com.likelion.hufsting.domain.Member.security.filter;

import com.likelion.hufsting.domain.Member.domain.Member;
import com.likelion.hufsting.domain.Member.exception.JwtAuthenticationException;
import com.likelion.hufsting.domain.Member.repository.MemberRepository;
import com.likelion.hufsting.domain.Member.security.GoogleOauthMemberDetails;
import com.likelion.hufsting.domain.Member.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OauthJwtAuthorizationFilter extends OncePerRequestFilter {
    // constant
    private final String INVALID_ACCESS_TOKEN_MSG = "유효하지 않은 액세스 토큰입니다.";
    // DI
    private final JwtUtil jwtUtil;
    // dependency injection
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
        System.out.println(request.getRequestURI());
        System.out.println(request.getMethod());
        if(isRequireFiltering(request)) {
            // get access token
            String accessToken = jwtUtil.getAccessToken(request);
            // validation accessToken
            if(!jwtUtil.validateToken(accessToken)){
                throw new JwtAuthenticationException(INVALID_ACCESS_TOKEN_MSG);
            }
            // validation success
            String email = jwtUtil.extractEmail(accessToken);
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new JwtAuthenticationException(INVALID_ACCESS_TOKEN_MSG));
            GoogleOauthMemberDetails googleOauthMemberDetails = new GoogleOauthMemberDetails(member);
            Authentication authentication = new UsernamePasswordAuthenticationToken(googleOauthMemberDetails, null, googleOauthMemberDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private Boolean isRequireFiltering(HttpServletRequest request){
        System.out.println("여기");
        return hasAccessToken(request) && isRequireCheckingRequest(request);
    }

    // 액세스 토큰 여부 확인
    private Boolean hasAccessToken(HttpServletRequest request){
        String accessToken = jwtUtil.getAccessToken(request);
        System.out.println(accessToken);
        return accessToken != null;
    }

    // 요청(Request) 필터링 여부 확인
    private Boolean isRequireCheckingRequest(HttpServletRequest request){
        // local variable
        List<String> requireCheckingMethod = List.of("POST", "PUT", "PATCH", "DELETE");
        List<String> requireCheckingUri = List.of(
                "/api/v1/my-matchingposts",
                "/api/v1/my-matchingrequests",
                "/api/v1/alarms"
        );
        // get various value
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        System.out.println(requestMethod.equals("GET") && requireCheckingUri.contains(requestUri));
        // post, put, patch, delete 요청 확인
        // 허용되지 않은 get 요청 확인
        System.out.println(requestUri);
        System.out.println(requestMethod);
        return requireCheckingMethod.contains(requestMethod) ||
                (requestMethod.equals("GET") && requireCheckingUri.contains(requestUri));
    }
}
