package com.example.kyc.security.filter;


import com.example.kyc.security.JwtAuthenticationToken;
import com.example.kyc.security.JwtInfoExtractor;
import com.example.kyc.security.JwtValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtValidator jwtValidator;
    private final JwtInfoExtractor jwtInfoExtractor;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = getTokenFromRequest((HttpServletRequest) request);

        if(isTokenValid(request, accessToken)) {
            setAuthentication(accessToken, request);
        }

        chain.doFilter(request, response);
    }

    private boolean isTokenValid(ServletRequest request, String accessToken) {
        return accessToken != null && jwtValidator.validateToken(request, accessToken);
    }

    private void setAuthentication(String accessToken, ServletRequest request) {
        Claims claims = jwtInfoExtractor.extractToken(accessToken);
        UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(claims, request);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private JwtAuthenticationToken getAuthenticationToken(Claims claims, ServletRequest request) {
        String username = claims.getSubject();
        List<GrantedAuthority> authorities = Collections.emptyList();
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(username, null, authorities, claims);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) request));
        return authenticationToken;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}