package com.example.emotrak.jwt;

import com.example.emotrak.exception.CustomErrorCode;
import com.example.emotrak.exception.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = jwtUtil.resolveToken(request);

        if(token != null) {
            if(!jwtUtil.validateToken(token)){
                jwtExceptionHandler(response, HttpStatus.UNAUTHORIZED.value());
                return;
            }
            Claims info = jwtUtil.getUserInfoFromToken(token);
            setAuthentication(info.getSubject());
        }
        filterChain.doFilter(request,response);
    }

    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtUtil.createAuthentication(username); // 인증 객체를 만든다
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    public void jwtExceptionHandler(HttpServletResponse response, int statusCode) {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(new ResponseMessage(CustomErrorCode.INVALID_TOKEN));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}